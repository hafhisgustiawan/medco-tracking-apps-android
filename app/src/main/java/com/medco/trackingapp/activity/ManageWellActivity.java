package com.medco.trackingapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.medco.trackingapp.R;
import com.medco.trackingapp.adapter.ImageAdapter;
import com.medco.trackingapp.databinding.ActivityManageWellBinding;
import com.medco.trackingapp.fragment.SetLocationFragment;
import com.medco.trackingapp.helper.CustomException;
import com.medco.trackingapp.helper.EtWatcher;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.ImageItem;
import com.medco.trackingapp.model.WellItem;
import com.medco.trackingapp.model.viewmodel.ManageWellViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ManageWellActivity extends BaseActivity {

	public static final String TAG = "ManageWellActivity";
	private Context mContext;
	private FragmentManager fragmentManager;
	private ActivityManageWellBinding binding;
	private SnackbarHelper snackbarHelper;
	private ImageAdapter adapter;
	private CollectionReference wellColl;
	private StorageReference storageReference;
	private int selectedItemPosition;
	private DocumentReference currentWellRef;
	private ManageWellViewModel mViewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		mContext = this;
		fragmentManager = getSupportFragmentManager();
		binding = ActivityManageWellBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		snackbarHelper = new SnackbarHelper(findViewById(android.R.id.content), null);

		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		wellColl = firebaseFirestore.collection(getString(R.string.collection_well));

		FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
		storageReference = firebaseStorage.getReference(getString(R.string.collection_well));

		String path = getIntent().getStringExtra("path");
		if (path != null) {
			currentWellRef = firebaseFirestore.document(path);
			binding.setCurrentWellRef(currentWellRef);
		}

		mViewModel = new ViewModelProvider(this).get(ManageWellViewModel.class);

		checkPermission();
	}

	@SuppressLint("InlinedApi")
	private void checkPermission() {
		if (!isPermissionGranted()) {
			String[] permitStr = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
				new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission
					.READ_MEDIA_IMAGES} : new String[]{Manifest.permission
				.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
			permissionLauncher.launch(permitStr);
			return;
		}
		initViews();
		initListeners();
	}

	@Override
	public void initViews() {
		mViewModel.getManageWellState().observe(this, item -> binding.setWellItem(item));
		initRecyclerView(); //include next step
	}

	@Override
	public void initListeners() {
		binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher()
			.onBackPressed());

		binding.etName.addTextChangedListener(new EtWatcher(str -> {
			WellItem item = mViewModel.getManageWellState().getValue();
			if (item == null) return;
			item.setName(str);
			mViewModel.setManageWellState(item);
		}));

		binding.etCategories.setOnClickListener(view -> {
			final String[] option = new String[3];
			option[0] = "AS (Alur Siwah)";
			option[1] = "JR (Julok Rayeuk)";
			option[2] = "Other Well (Sumur Tua)";

			List<String> realCat = new ArrayList<>();
			realCat.add("as");
			realCat.add("jr");
			realCat.add("otherwell");

			new AlertDialog.Builder(mContext)
				.setTitle("Pilih Kategori")
				.setItems(option, (dialog, which) -> {
					WellItem item = mViewModel.getManageWellState().getValue();
					if (item == null) return;
					item.setCategory(realCat.get(which));
					binding.etCategories.setText(option[which]);
				}).create().show();
		});

		binding.etDescription.addTextChangedListener(new EtWatcher(str -> {
			WellItem item = mViewModel.getManageWellState().getValue();
			if (item == null) return;
			item.setDescription(str);
			mViewModel.setManageWellState(item);
		}));

		binding.tvLocation.setOnClickListener(view -> {
			SetLocationFragment fragment = new SetLocationFragment();
			fragment.setCancelable(false);
			fragment.ListenerApiClose(fragment::dismiss);
			fragment.ListenerApiUpdate((lat, lng, add) -> {
				WellItem item = mViewModel.getManageWellState().getValue();
				if (item == null) return;
				item.setLocation(new GeoPoint(lat, lng));
				mViewModel.setManageWellState(item);
				binding.etAddress.setText(add);
				fragment.dismiss();
			});
			if (!fragmentManager.isDestroyed()) fragment.show(fragmentManager, TAG);
		});

		/*binding.etLatitude.addTextChangedListener(new EtWatcher(str -> {
			WellItem item = mViewModel.getManageWellState().getValue();

			if (str == null || str.equals("")) return;

			if (item == null || !TextUtils.isDigitsOnly(str.trim())) return;

			GeoPoint geoPoint = item.getLocation();

			if (geoPoint == null) {
				geoPoint = new GeoPoint(Double.parseDouble(str.trim()), 0);
			} else {
				double longitude = geoPoint.getLongitude();
				geoPoint = new GeoPoint(Double.parseDouble(str.trim()), longitude);
			}

			item.setLocation(geoPoint);
			mViewModel.setManageWellState(item);
		}));

		binding.etLongitude.addTextChangedListener(new EtWatcher(str -> {
			WellItem item = mViewModel.getManageWellState().getValue();

			if (str == null || str.equals("")) return;
			if (item == null || !TextUtils.isDigitsOnly(str.trim())) return;

			if (item.getLocation().getLongitude() < -90) {
				str = "-90";
			}

			if (item.getLocation().getLongitude() > 90) {
				str = "90";
			}

			GeoPoint geoPoint = item.getLocation();

			if (geoPoint == null) {
				geoPoint = new GeoPoint(0, Double.parseDouble(str.trim()));
			} else {
				double latitude = geoPoint.getLatitude();
				geoPoint = new GeoPoint(latitude, Double.parseDouble(str.trim()));
			}

			item.setLocation(geoPoint);
			mViewModel.setManageWellState(item);
		}));*/

		binding.etAddress.addTextChangedListener(new EtWatcher(str -> {
			WellItem item = mViewModel.getManageWellState().getValue();
			if (item == null) return;
			item.setAddress(str);
			mViewModel.setManageWellState(item);
		}));

		binding.btnSave.setOnClickListener(view -> {
			if (adapter == null || adapter.getData() == null || adapter.getData().size() == 1) {
				showError(new CustomException("Anda belum menambahkan foto sumur",
					new Throwable()));
				return;
			}

			String message = currentWellRef == null ?
				"Anda yakin ingin menambahkan data sumur baru?" :
				"Anda yakin ingin memperbarui data sumur ini?";

			new AlertDialog.Builder(mContext)
				.setCancelable(true)
				.setMessage(message)
				.setNegativeButton("Tidak", null)
				.setPositiveButton("Ya", (dialogInterface, i) -> {
					if (currentWellRef == null) {
						addData();
					} else {
						deleteNotExistingData();
					}
				})
				.create().show();

		});
	}

	private void deleteNotExistingData() {
		if (currentWellRef == null) {
			showError(new CustomException("Referensi sumur tidak ditemukan", new Throwable()));
			return;
		}

		WellItem item = mViewModel.getManageWellState().getValue();
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

	private void manipulateTasks(WellItem item) {
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

	private void updateData(WellItem item) {
		showProgress();
		currentWellRef.set(item)
			.addOnCompleteListener(t -> {
				dismissProgress();
				if (!t.isSuccessful()) {
					showError(t.getException());
					return;
				}
				Intent intent = new Intent(mContext, WellActivity.class);
				intent.putExtra("path", currentWellRef.getPath());
				startActivity(intent);
				finish();
			});
	}

	private void addData() {
		WellItem item = mViewModel.getManageWellState().getValue();
		if (item == null) return;
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

			wellColl.add(item)
				.addOnCompleteListener(t -> {
					dismissProgress();
					if (!t.isSuccessful()) {
						showError(t.getException());
						return;
					}
					Intent intent = new Intent(mContext, WellActivity.class);
					intent.putExtra("path", t.getResult().getPath());
					startActivity(intent);
					finish();
				});
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

		if (currentWellRef == null) {
			adapter.addData(null);
			return;
		}
		initAllData();
	}

	private void initAllData() {
		showProgress();
		currentWellRef.get().addOnCompleteListener(task -> {
			dismissProgress();
			if (!task.isSuccessful()) {
				showError(task.getException());
				return;
			}

			WellItem wellItem = task.getResult().toObject(WellItem.class);
			if (wellItem == null) return;
			mViewModel.setManageWellState(wellItem);

			if (wellItem.getImages() == null || adapter == null) return;
			wellItem.getImages().forEach(s -> {
				ImageItem item = new ImageItem();
				item.setImage(s);
				item.setType(getString(R.string.collection_well));
				adapter.addData(item);
			});
			adapter.addData(null);
		});
	}

	private void openFileChooser() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		pickPhotoLauncher.launch(Intent.createChooser(intent, "Pilih foto sumur"));
	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	private void showProgress() {
		binding.setLoading(true);
		binding.btnSave.setEnabled(false);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("SetTextI18n")
	private void dismissProgress() {
		binding.setLoading(false);
		binding.btnSave.setEnabled(true);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	public String getUniqueID() {
		String uniqueID = UUID.randomUUID().toString().replace("-", "");
		return uniqueID.substring(0, 20);
	}

	private String getFileExtension(Uri uri) {
		ContentResolver cR = getContentResolver();
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		return mime.getExtensionFromMimeType(cR.getType(uri));
	}

	@SuppressLint("InlinedApi")
	private boolean isPermissionGranted() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			return ActivityCompat.checkSelfPermission(mContext, Manifest.permission
				.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED && ActivityCompat
				.checkSelfPermission(mContext, Manifest.permission.READ_MEDIA_VIDEO) ==
				PackageManager.PERMISSION_GRANTED;
		} else {
			return ActivityCompat.checkSelfPermission(mContext, Manifest.permission
				.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat
				.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
				PackageManager.PERMISSION_GRANTED;
		}
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
}