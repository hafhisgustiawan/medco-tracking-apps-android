package com.medco.trackingapp.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.medco.trackingapp.BuildConfig;
import com.medco.trackingapp.R;
import com.medco.trackingapp.adapter.ImageAdapter;
import com.medco.trackingapp.databinding.FragmentManageReportBinding;
import com.medco.trackingapp.helper.CustomException;
import com.medco.trackingapp.helper.EtWatcher;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.ImageItem;
import com.medco.trackingapp.model.ReportItem;
import com.medco.trackingapp.model.WellItem;
import com.medco.trackingapp.model.direction.DirectionResponse;
import com.medco.trackingapp.model.direction.LegsItem;
import com.medco.trackingapp.model.direction.RoutesItem;
import com.medco.trackingapp.model.viewmodel.ManageReportViewModel;
import com.medco.trackingapp.service.MapsApiClient;
import com.medco.trackingapp.service.MapsApiInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ManageReportFragment extends BottomSheetDialogFragment {

	public static final String TAG = "ManageReportFragment";
	private Context mContext;
	private SnackbarHelper snackbarHelper;
	private FragmentManageReportBinding binding;
	private CollectionReference reportColl;
	private StorageReference storageReference;
	public DocumentSnapshot mReportSnapshot;
	private ManageReportViewModel mViewModel;
	private ImageAdapter adapter;
	private int selectedItemPosition;
	public DocumentReference mWellRef;
	private WellItem mWellItem;
	private DocumentReference currentUserRef;

	//location api
	private MapsApiInterface mapsApiInterface;
	private FusedLocationProviderClient mFusedLocationProviderClient;
	private LocationRequest locationRequest;
	private LocationCallback locationCallback;
	private Location mLocation;


	//callback
	public ListenerClose listenerClose;

	public void ListenerApiClose(ListenerClose listener) {
		listenerClose = listener;
	}

	public ManageReportFragment(DocumentReference wellRef, DocumentSnapshot reportSnapshot) {
		// Required empty public constructor
		this.mReportSnapshot = reportSnapshot;
		this.mWellRef = wellRef;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
		assert dialog != null;
		dialog.getBehavior().setPeekHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
		dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState) {
		mContext = requireContext();
		binding = FragmentManageReportBinding.inflate(inflater, container, false);

		snackbarHelper = new SnackbarHelper(Objects.requireNonNull(requireDialog()
			.getWindow()).getDecorView(), binding.spacer);

		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		reportColl = firebaseFirestore.collection(getString(R.string.collection_report));

		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser == null) return binding.getRoot();
		currentUserRef = firebaseFirestore.collection(getString(R.string.collection_user))
			.document(firebaseUser.getUid());

		FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
		storageReference = firebaseStorage.getReference(getString(R.string.collection_report));

		binding.setCurrentReportSnapshot(mReportSnapshot);

		mViewModel = new ViewModelProvider(this).get(ManageReportViewModel.class);

		mapsApiInterface = MapsApiClient.getClient().create(MapsApiInterface.class);

		mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
		locationRequest = new LocationRequest.Builder(Priority
			.PRIORITY_HIGH_ACCURACY, 30000)
			.setWaitForAccurateLocation(false)
			.setMinUpdateIntervalMillis(5000)
			.setMaxUpdateDelayMillis(100)
			.build();

		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(@NonNull LocationResult locationResult) {
				super.onLocationResult(locationResult);
				mLocation = locationResult.getLastLocation();
				if (mLocation == null) return;
				new Handler().postDelayed(() -> stopLocationUpdate(), 500);
			}
		};

		checkPermission();
		return binding.getRoot();
	}

	@SuppressLint("InlinedApi")
	private void checkPermission() {
		if (!isPermissionGranted()) {
			String[] permitStr = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
				new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission
					.READ_MEDIA_IMAGES, Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION} : new String[]{Manifest.permission
				.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission
				.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION};
			permissionLauncher.launch(permitStr);
			return;
		}
		startLocationUpdate();
		initViews();
		initListeners();
	}

	public void initViews() {
		mViewModel.getManageReportState().observe(getViewLifecycleOwner(), item -> binding
			.setReportItem(item));

		initRecyclerView(); //include next step

		ReportItem item;
		if (mReportSnapshot != null) {
			item = mReportSnapshot.toObject(ReportItem.class);
			if (item == null) return;
		} else {
			item = mViewModel.getManageReportState().getValue();
			if (item == null) return;

			item.setUserRef(currentUserRef);
			item.setWellRef(mWellRef);

		}
		mViewModel.setManageReportState(item);

		if (mWellRef != null) {
			mWellRef.get().addOnCompleteListener(task -> {
				if (!task.isSuccessful()) return;
				mWellItem = task.getResult().toObject(WellItem.class);
			});
		}
	}

	public void initListeners() {
		binding.tvClose.setOnClickListener(view -> {
			if (listenerClose == null) return;
			listenerClose.listenerClose(0);
		});

		binding.etReport.addTextChangedListener(new EtWatcher(str -> {
			ReportItem item = mViewModel.getManageReportState().getValue();
			if (item == null) return;
			item.setReport(str);
			mViewModel.setManageReportState(item);
		}));

		binding.etCondition.addTextChangedListener(new EtWatcher(str -> {
			ReportItem item = mViewModel.getManageReportState().getValue();
			if (item == null) return;
			item.setCondition(str);
			mViewModel.setManageReportState(item);
		}));

		binding.etNote.addTextChangedListener(new EtWatcher(str -> {
			ReportItem item = mViewModel.getManageReportState().getValue();
			if (item == null) return;
			item.setNote(str);
			mViewModel.setManageReportState(item);
		}));

		binding.btnSave.setOnClickListener(view -> {
			if (mWellItem == null) return;
			if (adapter == null || adapter.getData() == null || adapter.getData().size() == 1) {
				showError(new CustomException("Anda belum menambahkan foto laporan", new Throwable()));
				return;
			}

			String message = mReportSnapshot == null ?
				"Anda yakin ingin menambahkan data laporan baru?" :
				"Anda yakin ingin memperbarui data laporan ini?";

			new AlertDialog.Builder(mContext)
				.setCancelable(true)
				.setMessage(message)
				.setNegativeButton("Tidak", null)
				.setPositiveButton("Ya", (dialogInterface, i) -> {
					if (mReportSnapshot == null) {
						detectLimitDistance();
					} else {
						deleteNotExistingData();
					}
				})
				.create().show();
		});
	}

	private void detectLimitDistance() {
		if (mLocation == null) {
			showError(new CustomException("Lokasi anda tidak terdeteksi", new Throwable()));
			return;
		}

		if (mWellItem == null || mWellItem.getLocation() == null) {
			showError(new CustomException("Lokasi sumur tidak ditemukan", new Throwable()));
			return;
		}

		showProgress();
		mapsApiInterface.getDirection("driving",
				true,
				mLocation.getLatitude() + ","
					+ mLocation.getLongitude(), mWellItem
					.getLocation().getLatitude() + "," + mWellItem
					.getLocation().getLongitude(),
				BuildConfig.MAPS_API_KEY)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new SingleObserver<DirectionResponse>() {
				@Override
				public void onSubscribe(Disposable d) {

				}

				@Override
				public void onSuccess(DirectionResponse directionResponse) {
					dismissProgress();
					if (directionResponse.getRoutes() == null || directionResponse.getRoutes()
						.size() == 0) {
						showError(new CustomException("Rute tidak ditemukan", new Throwable()));
						return;
					}

					List<RoutesItem> routeMapsItems = directionResponse.getRoutes();
					RoutesItem route = routeMapsItems.get(0);

					List<LegsItem> legs = route.getLegs();

					if (legs == null || legs.size() == 0) {
						showError(new CustomException("Rute tidak ditemukan", new Throwable()));
						return;
					}

					AtomicInteger totalDistance = new AtomicInteger();

					legs.forEach(legsItem -> totalDistance.addAndGet(legsItem.getDistance().getValue()));

					if (totalDistance.get() <= 1000) {
						addData();
					} else {
						String message = "Sistem mendeteksi bahwa jarak anda dan lokasi sumur lebih dari " +
							"1000 m, yaitu " + totalDistance + " m. Pastikan jarak anda berada maksimal 1000 m " +
							"dari lokasi sumur.";

						new AlertDialog.Builder(mContext)
							.setCancelable(true)
							.setMessage(message)
							.setNegativeButton("Oke", null)
							.create().show();
					}
				}

				@Override
				public void onError(Throwable e) {
					showErrorThrowable(e);
				}
			});
	}

	private void addData() {
		ReportItem item = mViewModel.getManageReportState().getValue();
		if (item == null) {
			showError(new CustomException("Data tidak valid", new Throwable()));
			return;
		}
		item.setCategory(mWellItem.getCategory());
		item.setCreatedAt(Timestamp.now());

		List<UploadTask> putFileTasks = new ArrayList<>();
		List<String> images = new ArrayList<>();
		adapter.getData().forEach(imageItem -> {
			if (imageItem != null && imageItem.getUri() != null) {
				String nameFile = getUniqueID() + "." + getFileExtension(imageItem.getUri());
				images.add(nameFile);
				StorageReference fileRef = storageReference.child(nameFile);
				putFileTasks.add(fileRef.putFile(imageItem.getUri()));
			}
		});
		item.setImages(images);
		if (putFileTasks.size() == 0) return;

		showProgress();
		Tasks.whenAllComplete(putFileTasks).addOnCompleteListener(task -> {
			if (!task.isSuccessful()) {
				dismissProgress();
				showError(task.getException());
				return;
			}

			reportColl.add(item)
				.addOnCompleteListener(t -> {
					dismissProgress();
					if (!t.isSuccessful()) {
						showError(t.getException());
						return;
					}
					if (listenerClose == null) return;
					listenerClose.listenerClose(1);
				});
		});
	}

	private void deleteNotExistingData() {
		if (mReportSnapshot == null) {
			showError(new CustomException("Referensi laporan tidak ditemukan", new Throwable()));
			return;
		}

		ReportItem item = mViewModel.getManageReportState().getValue();
		if (item == null) return;

		List<Task<Void>> deleteFileTasks = new ArrayList<>();
		List<String> imgExist = new ArrayList<>();
		adapter.getData().forEach(imageItem -> {
			if (imageItem != null && imageItem.getImage() != null) {
				imgExist.add(imageItem.getImage());
			}
		});

		item.getImages().forEach(s -> {
			if (!imgExist.contains(s)) {
				StorageReference fileRef = storageReference.child(s);
				deleteFileTasks.add(fileRef.delete());
			}
		});

		if (deleteFileTasks.size() == 0) {
			//skip ke proses selanjutnya
			manipulateTasks(item);
			return;
		}

		showProgress();
		Tasks.whenAllComplete(deleteFileTasks)
			.addOnCompleteListener(t -> {
				dismissProgress();
				if (!t.isSuccessful()) {
					showError(t.getException());
					return;
				}
				manipulateTasks(item);
			});
	}

	private void manipulateTasks(ReportItem item) {
		List<UploadTask> putFileTasks = new ArrayList<>();
		List<String> images = new ArrayList<>();
		AtomicInteger i = new AtomicInteger();
		adapter.getData().forEach(imageItem -> {
			if (imageItem != null) {
				if (imageItem.getUri() != null) {
					String nameFile = imageItem.getImage() == null ? getUniqueID() + "." +
						getFileExtension(imageItem.getUri()) : imageItem.getImage();

					images.add(nameFile);
					StorageReference fileRef = storageReference.child(nameFile);
					putFileTasks.add(fileRef.putFile(imageItem.getUri()));
				} else {
					images.add(imageItem.getImage());
				}
			}
			i.getAndIncrement();
		});
		item.setImages(images);
		if (putFileTasks.size() == 0) {
			updateData(item);
			return;
		}

		showProgress();
		Tasks.whenAllComplete(putFileTasks).addOnCompleteListener(task -> {
			dismissProgress();
			if (!task.isSuccessful()) {
				showError(task.getException());
				return;
			}

			updateData(item);
		});
	}

	private void updateData(ReportItem item) {
		showProgress();
		mReportSnapshot.getReference().set(item)
			.addOnCompleteListener(t -> {
				dismissProgress();
				if (!t.isSuccessful()) {
					showError(t.getException());
					return;
				}
				if (listenerClose == null) return;
				listenerClose.listenerClose(1);
			});
	}

	private void initRecyclerView() {
		adapter = new ImageAdapter(mContext);
		binding.rvImage.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager
			.HORIZONTAL, false));
		binding.rvImage.setAdapter(adapter);

		adapter.setOnItemClickListener(position -> {
			selectedItemPosition = position;
			openFileChooser();
		});

		if (mReportSnapshot == null) {
			adapter.addData(null);
			return;
		}
		initImagesData();
	}

	private void initImagesData() {
		ReportItem reportItem = mReportSnapshot.toObject(ReportItem.class);
		if (reportItem == null || reportItem.getImages() == null || adapter == null) return;
		reportItem.getImages().forEach(s -> {
			ImageItem item = new ImageItem();
			item.setImage(s);
			item.setType(getString(R.string.collection_report));
			adapter.addData(item);
		});
		adapter.addData(null);
	}

	private void openFileChooser() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		pickPhotoLauncher.launch(Intent.createChooser(intent, "Pilih foto laporan"));
	}

	private void showProgress() {
		binding.setLoading(true);
		binding.btnSave.setEnabled(false);
		requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("SetTextI18n")
	private void dismissProgress() {
		binding.setLoading(false);
		binding.btnSave.setEnabled(true);
		requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	private void showErrorThrowable(Throwable e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	public String getUniqueID() {
		String uniqueID = UUID.randomUUID().toString().replace("-", "");
		return uniqueID.substring(0, 20);
	}

	private String getFileExtension(Uri uri) {
		ContentResolver cR = mContext.getContentResolver();
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		return mime.getExtensionFromMimeType(cR.getType(uri));
	}

	@SuppressLint("InlinedApi")
	private boolean isPermissionGranted() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			return ActivityCompat.checkSelfPermission(mContext, Manifest.permission
				.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED && ActivityCompat
				.checkSelfPermission(mContext, Manifest.permission.READ_MEDIA_VIDEO) ==
				PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext,
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(mContext, Manifest.permission
					.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
		} else {
			return ActivityCompat.checkSelfPermission(mContext, Manifest.permission
				.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat
				.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
				PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext,
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
					== PackageManager.PERMISSION_GRANTED;
		}
	}

	@SuppressLint("MissingPermission")
	private void startLocationUpdate() {
		if (mFusedLocationProviderClient == null) return;
		mFusedLocationProviderClient.requestLocationUpdates(locationRequest,
			locationCallback, null);
	}

	private void stopLocationUpdate() {
		if (mFusedLocationProviderClient == null) return;
		mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
	}

	public ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
		new ActivityResultContracts.RequestMultiplePermissions(), result -> {
			Log.d(TAG, ": " + result.toString());
			if (result.containsValue(false)) {
				showError(new CustomException("Izinkan akses!", new Throwable()));
			} else {
				checkPermission();
			}
		});

	public ActivityResultLauncher<Intent> pickPhotoLauncher = registerForActivityResult(
		new ActivityResultContracts.StartActivityForResult(),
		result -> {
			if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null &&
				result.getData().getData() != null) {
				if (adapter == null) return;
				ImageItem item = adapter.getSelectedData(selectedItemPosition);
				item.setUri(result.getData().getData());
				adapter.updateData(item, selectedItemPosition);
				if (adapter.getItemCount() == 0) return;
				if (selectedItemPosition == adapter.getItemCount() - 1) adapter.addData(null);
			}
		}
	);

	public interface ListenerClose {
		void listenerClose(int selector); // 0 -> close dan 1 > reload parent
	}
}