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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.medco.trackingapp.R;
import com.medco.trackingapp.adapter.ImageAdapter;
import com.medco.trackingapp.databinding.ActivityManageWellBinding;
import com.medco.trackingapp.helper.CustomException;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.ImageItem;
import com.medco.trackingapp.model.WellItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
	private WellItem mWellItem;

	//value
	private String selectedCat;

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

		FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
		storageReference = firebaseStorage.getReference(getString(R.string.collection_well));

		String path = getIntent().getStringExtra("path");
		if (path != null) {
			currentWellRef = firebaseFirestore.document(path);
		}

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
		initRecyclerView(); //include next step
	}

	@Override
	public void initListeners() {
		binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher()
			.onBackPressed());

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
					selectedCat = realCat.get(which);
					binding.etCategories.setText(option[which]);
				}).create().show();
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

			mWellItem = task.getResult().toObject(WellItem.class);
			if (mWellItem == null) return;
			binding.setWellItem(mWellItem);
			selectedCat = mWellItem.getCategory();

			if (mWellItem.getImages() == null || adapter == null) return;
			mWellItem.getImages().forEach(s -> {
				ImageItem item = new ImageItem();
				item.setImage(s);
				adapter.addData(item);
			});
			adapter.addData(null);
		});
	}

	private void openFileChooser() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		pickPhotoLauncher.launch(Intent.createChooser(intent, "Pilih foto barang"));
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

	ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
		new ActivityResultContracts.RequestMultiplePermissions(), result -> {
			Log.d(TAG, ": " + result.toString());
			if (result.containsValue(false)) {
				showError(new CustomException("Izinkan akses!", new Throwable()));
			} else {
				checkPermission();
			}
		});

	ActivityResultLauncher<Intent> pickPhotoLauncher = registerForActivityResult(
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