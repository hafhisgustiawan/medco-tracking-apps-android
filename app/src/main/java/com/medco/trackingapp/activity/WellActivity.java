package com.medco.trackingapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medco.trackingapp.BuildConfig;
import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.ActivityWellBinding;
import com.medco.trackingapp.fragment.CautionFragment;
import com.medco.trackingapp.helper.CustomException;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.ResultMapsDistanceItem;
import com.medco.trackingapp.model.WellItem;
import com.medco.trackingapp.service.MapsApiClient;
import com.medco.trackingapp.service.MapsApiInterface;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WellActivity extends BaseActivity {

	private static final String TAG = "WellActivity";
	private Context mContext;
	private ActivityWellBinding binding;
	private Animation animation;
	private SnackbarHelper snackbarHelper;
	private FragmentManager fragmentManager;
	private CollectionReference reportColl;
	private DocumentReference currentUserRef;
	private DocumentReference currentWellRef;
	private WellItem mWellItem;

	//api
	private MapsApiInterface mapsApiInterface;

	//location
	private Location mLocation;
	private FusedLocationProviderClient fusedLocationProviderClient;
	private LocationRequest locationRequest;
	private LocationCallback locationCallback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		fragmentManager = getSupportFragmentManager();
		binding = ActivityWellBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
		snackbarHelper = new SnackbarHelper(findViewById(android.R.id.content), null);

		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser == null) return;
		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		reportColl = firebaseFirestore.collection(getString(R.string.collection_report));
		currentUserRef = firebaseFirestore.collection(getString(R.string.collection_user))
			.document(firebaseUser.getUid());

		Intent intent = getIntent();
		String path = intent.getStringExtra("path");
		if (path == null) return;
		currentWellRef = firebaseFirestore.document(path);

		mapsApiInterface = MapsApiClient.getClient().create(MapsApiInterface.class);

		fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
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
				//save the location
				mLocation = locationResult.getLastLocation();
				if (mLocation != null) {
					getDistance(mLocation);
				}

				new Handler().postDelayed(() -> stopLocationUpdate(), 500);
			}
		};

		initViews();
		initListeners();
		checkPermission();
	}

	private void checkPermission() {
		if (isPermissionGranted()) {
			startLocationUpdate();
			return;
		}

		String[] permitStr = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION};
		permissionLauncher.launch(permitStr);
	}

	@Override
	public void initViews() {
		binding.setUserRef(currentUserRef);

		showProgress();
		currentWellRef.get().addOnCompleteListener(tsk -> {
			dismissProgress();
			if (!tsk.isSuccessful()) {
				showError(tsk.getException());
				return;
			}

			if (!tsk.getResult().exists()) {
				showDeletedView();
				return;
			}

			mWellItem = tsk.getResult().toObject(WellItem.class);
			binding.setWellItem(mWellItem);

//			initRecyclerReport();
		});
	}

	@SuppressLint("NonConstantResourceId")
	@Override
	public void initListeners() {
		binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

		binding.tvCoordinates.setOnClickListener(view -> {
			if (mWellItem == null) return;
			if (mWellItem.getLocation() == null) return;
			Uri uri = Uri.parse("google.navigation:q=" + mWellItem.getLocation()
				.getLatitude() + "," + mWellItem.getLocation().getLongitude());
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.setPackage("com.google.android.apps.maps");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				snackbarHelper.show("Tidak dapat membuka google maps", Snackbar
					.LENGTH_INDEFINITE);
			}
		});

		binding.btnMoreOption.setOnClickListener(view -> {
			if (currentWellRef == null) return;
			PopupMenu popupMenu = new PopupMenu(mContext, binding.btnMoreOption);
			popupMenu.inflate(R.menu.delete_edit_menu);
			popupMenu.setOnMenuItemClickListener(item -> {
				switch (item.getItemId()) {
					case R.id.action_edit:
						Intent intent = new Intent(mContext, ManageWellActivity.class);
						intent.putExtra("path", currentWellRef.getPath());
						startActivity(intent);
						break;
					case R.id.action_delete:
						new AlertDialog.Builder(mContext)
							.setMessage("Anda yakin ingin menghapus data sumur ini?")
							.setNegativeButton("Tidak", null)
							.setPositiveButton("Ya", (dialogInterface, i) ->
								deleteWellData()).create().show();
						break;
				}
				return false;
			});
			popupMenu.show();
		});
	}

	private void deleteWellData() {
		if (currentWellRef == null) return;
		showProgress();
		reportColl.whereEqualTo("wellRef", currentWellRef).get()
			.addOnCompleteListener(task -> {
				dismissProgress();
				if (!task.isSuccessful()) {
					showError(task.getException());
					return;
				}
				if (!task.getResult().isEmpty()) {
					snackbarHelper.show("Data sumur ini tidak boleh dihapus ketika " +
							"sudah tertaut dengan laporan pemantauan",
						Snackbar.LENGTH_INDEFINITE);
					return;
				}
				showProgress();
				currentWellRef.delete().addOnCompleteListener(tsk -> {
					dismissProgress();
					if (!tsk.isSuccessful()) {
						showError(tsk.getException());
						return;
					}
					initViews();
				});
			});
	}

	private void showDeletedView() {
		if (isFinishing() || fragmentManager.isDestroyed()) {
			Toast.makeText(mContext, "Data sumur sudah dihapus!", Toast.LENGTH_SHORT)
				.show();
			getOnBackPressedDispatcher().onBackPressed();
			return;
		}

		CautionFragment fragment = new CautionFragment(R.raw.delete,
			"Paket wisata sudah dihapus!");
		fragment.setCancelable(false);
		fragment.ListenerApiClose(() -> getOnBackPressedDispatcher().onBackPressed());
		fragment.show(fragmentManager, TAG);
	}

	private void getDistance(Location location) {
		if (mWellItem == null) return;
		if (mWellItem.getLocation() == null) return;
		mapsApiInterface.getDistance(BuildConfig.MAPS_API_KEY, location.getLatitude() + ","
				+ location.getLongitude(), mWellItem.getLocation()
				.getLatitude() + "," + mWellItem.getLocation()
				.getLongitude())
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new SingleObserver<ResultMapsDistanceItem>() {
				@Override
				public void onSubscribe(@NonNull Disposable d) {

				}

				@SuppressLint("SetTextI18n")
				@Override
				public void onSuccess(@NonNull ResultMapsDistanceItem resultMapsDistanceItem) {

					ResultMapsDistanceItem.Distance distance = resultMapsDistanceItem.getRows()
						.get(0).getElements().get(0).getDistance();
					ResultMapsDistanceItem.Duration duration = resultMapsDistanceItem.getRows()
						.get(0).getElements().get(0).getDuration();

					if (distance == null || duration == null) return;

					binding.tvDistance.setText(distance.getText());
					binding.tvTravelingTime.setText(duration.getText());
				}

				@Override
				public void onError(@NonNull Throwable e) {
					showErrorThrowable(e);
				}
			});
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

	private void showProgress() {
		binding.progressbar.setVisibility(View.VISIBLE);
		binding.layoutMain.setVisibility(View.GONE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("SetTextI18n")
	private void dismissProgress() {
		binding.progressbar.setVisibility(View.GONE);
		binding.layoutMain.setAnimation(animation);
		binding.layoutMain.setVisibility(View.VISIBLE);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("MissingPermission")
	private void startLocationUpdate() {
		if (fusedLocationProviderClient == null) return;
		fusedLocationProviderClient.requestLocationUpdates(locationRequest,
			locationCallback, null);
	}

	private void stopLocationUpdate() {
		if (fusedLocationProviderClient == null) return;
		fusedLocationProviderClient.removeLocationUpdates(locationCallback);
	}

	@SuppressLint("InlinedApi")
	private boolean isPermissionGranted() {
		return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
			== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext,
			Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}

	ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
		new ActivityResultContracts.RequestMultiplePermissions(), result -> {
			Log.d(TAG, ": " + result.toString());
			if (result.containsValue(false)) {
				showError(new CustomException("Izinkan akses!", new Throwable()));
			} else {
				checkPermission();
			}
		});


}