package com.medco.trackingapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.PolyUtil;
import com.medco.trackingapp.BuildConfig;
import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.ActivityDirectionBinding;
import com.medco.trackingapp.helper.CustomException;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.WellItem;
import com.medco.trackingapp.model.direction.DirectionResponse;
import com.medco.trackingapp.model.direction.RoutesItem;
import com.medco.trackingapp.service.MapsApiClient;
import com.medco.trackingapp.service.MapsApiInterface;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DirectionActivity extends BaseActivity implements OnMapReadyCallback {

	public static final String TAG = "DirectionActivity";
	private Context mContext;
	private ActivityDirectionBinding binding;
	private SnackbarHelper snackbarHelper;
	private Animation animation;
	private WellItem mWellItem;
	private GoogleMap mMap;
	private LatLng myLatLng;
	private Marker myLocationMarker;
	private Polyline mPolyline;
	private DocumentReference mWellRef;

	//location api
	private MapsApiInterface mapsApiInterface;
	private FusedLocationProviderClient mFusedLocationProviderClient;
	private LocationRequest locationRequest;
	private LocationCallback locationCallback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		mContext = this;
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
		snackbarHelper = new SnackbarHelper(findViewById(android.R.id.content), null);
		binding = ActivityDirectionBinding.inflate(getLayoutInflater());
		binding.mapView.onCreate(savedInstanceState);
		setContentView(binding.getRoot());

		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		String path = getIntent().getStringExtra("path");
		if (path == null) return;
		mWellRef = firebaseFirestore.document(path);

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
				Location location = locationResult.getLastLocation();
				if (location == null || mMap == null || mWellItem == null) return;

				myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
				if (myLocationMarker != null) {
					myLocationMarker.setPosition(myLatLng);
				} else {
					myLocationMarker = mMap.addMarker(new MarkerOptions()
						.position(myLatLng).title("Lokasi saya")
						.icon(BitmapFromVector(mContext, R.drawable
							.baseline_location_pin_blue_24)));
				}

				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 12.0f));
				getDirectionPoly();
				new Handler().postDelayed(() -> stopLocationUpdate(), 500);
			}
		};

		checkPermission();
		initListeners();
	}

	private void checkPermission() {
		if (isPermissionGranted()) {
			binding.mapView.getMapAsync(this);
			return;
		}

		String[] permitStr = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION};
		permissionLauncher.launch(permitStr);
	}

	@SuppressLint("MissingPermission")
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		Log.d(TAG, "startLocationUpdate: ON MAP READY");
		mMap = googleMap;
		mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mContext, R.raw.map_style));
		mMap.getUiSettings().setMapToolbarEnabled(false);
		mMap.setMyLocationEnabled(true);

		initViews();
	}

	@Override
	public void initViews() {
		showProgress();
		mWellRef.get().addOnCompleteListener(task -> {
			dismissProgress();
			if (!task.isSuccessful()) {
				showError(task.getException());
				return;
			}
			mWellItem = task.getResult().toObject(WellItem.class);
			if (mWellItem == null) return;

			LatLng endLatLng = new LatLng(mWellItem.getLocation().getLatitude(), mWellItem
				.getLocation().getLongitude());
			mMap.addMarker(new MarkerOptions()
				.position(endLatLng).title(mWellItem.getName()));
		});
	}

	@Override
	public void initListeners() {
		binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
	}

	private void getDirectionPoly() {
		if (myLatLng == null || mWellItem == null) {
			binding.setIsRouteAvailable(false);
			return;
		}
		mapsApiInterface.getDirection("driving",
				true,
				myLatLng.latitude + ","
					+ myLatLng.longitude, mWellItem
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
					if (mMap == null) return;
					if (directionResponse.getRoutes() == null || directionResponse.getRoutes()
						.size() == 0) {
						binding.setIsRouteAvailable(false);
						showError(new CustomException("Rute tidak ditemukan", new Throwable()));
						return;
					}

					if (mPolyline != null) mPolyline.remove();

					List<RoutesItem> routeMapsItems = directionResponse.getRoutes();
					RoutesItem route = routeMapsItems.get(0);
					if (route == null || route.getOverviewPolyline() == null) return;
					String polyline = route.getOverviewPolyline()
						.getPoints();

					PolylineOptions polylineOptions = new PolylineOptions();

					polylineOptions.color(mContext.getColor(R.color.colorAccent));
					polylineOptions.width(18.0f);

					polylineOptions.startCap(new ButtCap())
						.jointType(JointType.ROUND).addAll(PolyUtil.decode(polyline));
					mPolyline = mMap.addPolyline(polylineOptions);
					binding.setIsRouteAvailable(true);
				}

				@Override
				public void onError(Throwable e) {
					binding.setIsRouteAvailable(false);
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
		binding.mapView.setVisibility(View.GONE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("SetTextI18n")
	private void dismissProgress() {
		binding.progressbar.setVisibility(View.GONE);
		binding.mapView.setAnimation(animation);
		binding.mapView.setVisibility(View.VISIBLE);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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

	@SuppressLint("InlinedApi")
	private boolean isPermissionGranted() {
		return ActivityCompat.checkSelfPermission(mContext, Manifest.permission
			.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat
			.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
			PackageManager.PERMISSION_GRANTED;
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

	@Override
	public void onStart() {
		super.onStart();
		binding.mapView.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		binding.mapView.onResume();
		startLocationUpdate();
	}

	@Override
	public void onPause() {
		super.onPause();
		binding.mapView.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		binding.mapView.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		binding.mapView.onDestroy();
		stopLocationUpdate();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		binding.mapView.onSaveInstanceState(outState);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		binding.mapView.onLowMemory();
	}

	private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
		// below line is use to generate a drawable.
		Drawable vectorDrawable = ContextCompat.getDrawable(
			context, vectorResId);
		if (vectorDrawable == null) return null;
		// below line is use to set bounds to our vector
		// drawable.
		vectorDrawable.setBounds(
			0, 0, vectorDrawable.getIntrinsicWidth(),
			vectorDrawable.getIntrinsicHeight());

		// below line is use to create a bitmap for our
		// drawable which we have added.
		Bitmap bitmap = Bitmap.createBitmap(
			vectorDrawable.getIntrinsicWidth(),
			vectorDrawable.getIntrinsicHeight(),
			Bitmap.Config.ARGB_8888);

		// below line is use to add bitmap in our canvas.
		Canvas canvas = new Canvas(bitmap);

		// below line is use to draw our
		// vector drawable in canvas.
		vectorDrawable.draw(canvas);

		// after generating our bitmap we are returning our
		// bitmap.
		return BitmapDescriptorFactory.fromBitmap(bitmap);
	}
}