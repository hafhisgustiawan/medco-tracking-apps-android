package com.medco.trackingapp.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.medco.trackingapp.BuildConfig;
import com.medco.trackingapp.R;
import com.medco.trackingapp.adapter.SuggestMapsAdapter;
import com.medco.trackingapp.databinding.FragmentSetLocationBinding;
import com.medco.trackingapp.helper.CustomException;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.ResultMapsPlaceItem;
import com.medco.trackingapp.service.MapsApiClient;
import com.medco.trackingapp.service.MapsApiInterface;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetLocationFragment extends BottomSheetDialogFragment
	implements OnMapReadyCallback {
	public static final String TAG = "Set Location Fragment";
	//callback
	public ListenerUpdate listenerUpdate;
	public ListenerClose listenerClose;
	private Context mContext;
	private FragmentSetLocationBinding binding;
	private SnackbarHelper snackbarHelper;
	//global value
	private GoogleMap mMap;
	private LatLng myLatLng;
	private Marker myLocationMarker;
	//location
	private FusedLocationProviderClient fusedLocationProviderClient;
	private LocationRequest locationRequest;
	private LocationCallback locationCallback;
	//adapter
	private SuggestMapsAdapter adapter;
	//api
	private MapsApiInterface mapsApiInterface;

	public SetLocationFragment() {
		// Required empty public constructor
	}

	public void ListenerApiUpdate(ListenerUpdate listener) {
		listenerUpdate = listener;
	}

	public void ListenerApiClose(ListenerClose l) {
		listenerClose = l;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState) {
		mContext = requireContext();
		binding = FragmentSetLocationBinding.inflate(inflater, container, false);
		binding.mapView.onCreate(savedInstanceState);

		mapsApiInterface = MapsApiClient.getClient().create(MapsApiInterface.class);
		snackbarHelper = new SnackbarHelper(Objects.requireNonNull(requireDialog().getWindow())
			.getDecorView(), binding.mapView);

		fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
		locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,
			30000)
			.setWaitForAccurateLocation(false)
			.setMinUpdateIntervalMillis(5000)
			.setMaxUpdateDelayMillis(100)
			.build();

		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(@NonNull LocationResult locationResult) {
				super.onLocationResult(locationResult);
				//save the location
				Location location = locationResult.getLastLocation();
				if (location == null) return;
				myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

				if (myLocationMarker != null) {
					myLocationMarker.setPosition(myLatLng);
				} else {
					myLocationMarker = mMap.addMarker(new MarkerOptions()
						.position(myLatLng));
				}

				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 12.0f));
				myLocationMarker.setDraggable(true);
				new Handler().postDelayed(() -> stopLocationUpdate(), 500);
				updateUI();
			}
		};

		checkPermission();
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
		assert dialog != null;
		dialog.getBehavior().setPeekHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
		dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
	}

	private void checkPermission() {
		if (!isPermissionLocationGranted()) {
			permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION});
			return;
		}
		binding.mapView.getMapAsync(this);
	}

	@SuppressLint("MissingPermission")
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		mMap = googleMap;
		mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mContext, R.raw.map_style));
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.setMyLocationEnabled(false);

		initViews();
		initListeners();
	}

	public void initViews() {
		startLocationUpdate();
	}

	@SuppressLint("PotentialBehaviorOverride")
	public void initListeners() {
		binding.btnClose.setOnClickListener(view -> listenerClose.listenerClose());

		mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
			@Override
			public void onMarkerDrag(@NonNull Marker marker) {

			}

			@Override
			public void onMarkerDragEnd(@NonNull Marker marker) {
				updateUI();
			}

			@Override
			public void onMarkerDragStart(@NonNull Marker marker) {

			}
		});

		binding.btnMyLocation.setOnClickListener(view -> {
			if (myLatLng == null) return;
			if (myLocationMarker != null) {
				myLocationMarker.setPosition(myLatLng);
			} else {
				myLocationMarker = mMap.addMarker(new MarkerOptions()
					.position(myLatLng));
			}

			if (myLocationMarker != null) {
				myLocationMarker.setDraggable(true);
			}
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 12.0f));
			updateUI();
		});

		binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				if (query.trim().length() != 0) {
					Geocoder geocoder = new Geocoder(mContext);
					List<Address> addressList;
					try {
						addressList = geocoder.getFromLocationName(query, 1);
						if (addressList == null) return false;
						LatLng myLocation = new LatLng(addressList.get(0).getLatitude(),
							addressList.get(0).getLongitude());
						if (myLocationMarker != null) {
							myLocationMarker.setPosition(myLocation);
						} else {
							myLocationMarker = mMap.addMarker(new MarkerOptions()
								.position(myLocation));

						}

						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12.0f));
						updateUI();

						if (myLocationMarker != null) {
							myLocationMarker.setDraggable(true);
						}
					} catch (Exception e) {
						showError(e);
					}
				}

				binding.layoutSuggest.setVisibility(View.GONE);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				if (newText.trim().length() != 0) {
					binding.layoutSuggest.setVisibility(View.VISIBLE);
					setUpRecyclerViewSuggest(newText);
				} else {
					binding.layoutSuggest.setVisibility(View.GONE);
				}
				return false;
			}
		});

		binding.tvSave.setOnClickListener(view -> {
			if (myLocationMarker == null || listenerUpdate == null) return;
			String address = binding.etAddress.getText() != null ? binding.etAddress.getText()
				.toString().trim() : "";
			listenerUpdate.listenerUpdate(myLocationMarker.getPosition().latitude, myLocationMarker
				.getPosition().longitude, address);
		});
	}

	private void setUpRecyclerViewSuggest(String keyWord) {
		mapsApiInterface.getPlace(keyWord, BuildConfig.MAPS_API_KEY)
			.enqueue(new Callback<ResultMapsPlaceItem>() {
				@Override
				public void onResponse(@NonNull Call<ResultMapsPlaceItem> call,
															 @NonNull Response<ResultMapsPlaceItem> response) {
					if (response.isSuccessful()) {
						if (response.body() != null && response.body().getPredictions() != null) {
							adapter = new SuggestMapsAdapter(mContext, response.body().getPredictions());
							binding.rvSuggest.setLayoutManager(new LinearLayoutManager(mContext));
							binding.rvSuggest.setAdapter(adapter);

							adapter.setOnItemClickListener((position, kw) -> binding.searchView
								.setQuery(kw, true));
						}
					} else {
						binding.rvSuggest.setVisibility(View.GONE);
					}
				}

				@Override
				public void onFailure(@NonNull Call<ResultMapsPlaceItem> call,
															@NonNull Throwable t) {
					showErrorThrowable(t);
					binding.layoutSuggest.setVisibility(View.GONE);
				}
			});
	}

	@SuppressLint("SetTextI18n")
	private void updateUI() {
		if (myLocationMarker == null) return;

		binding.etLatitude.setText(String.valueOf(myLocationMarker.getPosition().latitude));
		binding.etLongitude.setText(String.valueOf(myLocationMarker.getPosition().longitude));

		Geocoder geocoder = new Geocoder(mContext);
		try {
			List<Address> addressList = geocoder.getFromLocation(myLocationMarker.getPosition()
				.latitude, myLocationMarker.getPosition().longitude, 1);
			if (addressList == null) return;
			binding.etAddress.setText(addressList.get(0).getAddressLine(0));
		} catch (Exception e) {
			showError(e);
			binding.etAddress.setText("");
		}
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

	private boolean isPermissionLocationGranted() {
		return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
			== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext,
			Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}

	@SuppressLint("MissingPermission")
	private void startLocationUpdate() {
		if (isPermissionLocationGranted()) {
			fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
		}
	}

	private void stopLocationUpdate() {
		if (isPermissionLocationGranted()) {
			fusedLocationProviderClient.removeLocationUpdates(locationCallback);
		}
	}

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

	ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
		new ActivityResultContracts.RequestMultiplePermissions(), result -> {
			if (result.containsValue(false)) {
				//permission denied
				showError(new CustomException("Izinkan akses lokasi", new Throwable()));
				new Handler().postDelayed(() -> listenerClose.listenerClose(), 1000);
			} else {
				//permission granted
				checkPermission();
			}
		});

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

	public interface ListenerUpdate {
		void listenerUpdate(Double latitude, Double longitude, String address);
	}

	public interface ListenerClose {
		void listenerClose();
	}
}