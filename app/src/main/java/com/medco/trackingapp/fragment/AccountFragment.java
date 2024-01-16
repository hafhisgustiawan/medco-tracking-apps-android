package com.medco.trackingapp.fragment;

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
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.FragmentAccountBinding;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.UserItem;

public class AccountFragment extends BaseFragment {

	public static final String TAG = "Account Fragment";
	private Context mContext;
	private FragmentManager fragmentManager;
	private FragmentActivity mActivity;
	private FragmentAccountBinding binding;
	private SnackbarHelper snackbarHelper;

	//firebase
	private FirebaseAuth firebaseAuth;
	private FirebaseUser firebaseUser;
	private DocumentReference currentUserRef;
	private FirebaseStorage firebaseStorage;
	private StorageReference storageReference;

	//global value
	private Animation animation;
	private UserItem mUserItem;
	private Uri imageUri;

	public AccountFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState) {
		mContext = requireContext();
		mActivity = requireActivity();
		fragmentManager = getChildFragmentManager();
		binding = FragmentAccountBinding.inflate(inflater, container, false);
		snackbarHelper = new SnackbarHelper(mActivity.findViewById(android.R.id
			.content), binding.anchorPoint);
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);

		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();
		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		firebaseStorage = FirebaseStorage.getInstance();
		storageReference = firebaseStorage.getReference(getString(R.string
			.collection_user));

		if (firebaseUser == null) return binding.getRoot();

		currentUserRef = firebaseFirestore.collection(getString(R.string
			.collection_user)).document(firebaseUser.getUid());

		initViews();
		initListeners();
		return binding.getRoot();
	}

	@Override
	public void initViews() {
		showProgress();
		new Handler().postDelayed(this::initAllData, 500);
	}

	@SuppressLint("NonConstantResourceId")
	@Override
	public void initListeners() {
		binding.btnLogout.setOnClickListener(v -> new AlertDialog.Builder(mContext)
			.setCancelable(true)
			.setMessage("Anda ingin keluar?")
			.setNegativeButton("Tidak", null)
			.setPositiveButton("Ya", (dialogInterface, i) -> setLogOut()).create().show());

		binding.btnImg.setOnClickListener(v -> {
			PopupMenu popupMenu = new PopupMenu(mContext, binding.btnImg);
			popupMenu.inflate(R.menu.photo_menu);
			popupMenu.setOnMenuItemClickListener(item -> {
				switch (item.getItemId()) {
					case R.id.action_upload:
						checkPermission();
						break;
					case R.id.action_delete:
						if (mUserItem.getPhoto() != null) {
							new AlertDialog.Builder(mContext)
								.setCancelable(true)
								.setMessage("Anda yakin ingin hapus foto?")
								.setPositiveButton("Ya", (dialogInterface, i) ->
									deletePhoto())
								.setNegativeButton("Tidak", null)
								.create().show();
						}
						break;
				}
				return false;
			});
			popupMenu.show();
		});

		binding.layoutName.setOnClickListener(v -> {
			if (mUserItem == null) return;
			UpdatePrivateDataFragment fragment = new UpdatePrivateDataFragment(mUserItem, 0);
			fragment.setCancelable(false);
			fragment.ListenerApiClose(selector -> {
				fragment.dismiss();
				if (selector == 1) {
					initViews();
				}
			});
			if (!fragmentManager.isDestroyed()) fragment.show(fragmentManager, TAG);
		});

		binding.layoutEmail.setOnClickListener(v -> {
			if (mUserItem == null) return;
			UpdatePrivateDataFragment fragment = new UpdatePrivateDataFragment(mUserItem, 1);
			fragment.setCancelable(false);
			fragment.ListenerApiClose(selector -> {
				fragment.dismiss();
				if (selector == 1) {
					initViews();
					new AlertDialog.Builder(mContext)
						.setMessage("Email berhasil diperbarui, silahkan login kembali!")
						.setCancelable(false)
						.setNegativeButton("Tidak", null)
						.setPositiveButton("Ya", (dialogInterface, i) -> setLogOut())
						.setOnDismissListener(dialogInterface -> setLogOut()).create().show();
				}
			});
			if (!fragmentManager.isDestroyed()) fragment.show(fragmentManager, TAG);
		});

		binding.layoutPhone.setOnClickListener(v -> {
			if (mUserItem == null) return;
			UpdatePrivateDataFragment fragment = new UpdatePrivateDataFragment(mUserItem, 2);
			fragment.setCancelable(false);
			fragment.ListenerApiClose(selector -> {
				fragment.dismiss();
				if (selector == 1) {
					initViews();
				}
			});
			if (!fragmentManager.isDestroyed()) fragment.show(fragmentManager, TAG);
		});

		binding.tvResetPassword.setOnClickListener(view -> {
			ResetPasswordFragment fragment = new ResetPasswordFragment();
			fragment.setCancelable(false);
			fragment.ListenerApiClose(fragment::dismiss);
			if (!fragmentManager.isDestroyed()) fragment.show(fragmentManager, TAG);
		});

		binding.tvAbout.setOnClickListener(view -> {
			AboutFragment aboutFragment = new AboutFragment();
			if (!fragmentManager.isDestroyed()) aboutFragment.show(fragmentManager, TAG);
			aboutFragment.setCancelable(true);
			aboutFragment.ListenerApiClose(aboutFragment::dismiss);
		});
	}

	private void initAllData() {
		currentUserRef.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) {
				showError(task.getException());
				return;
			}

			if (!task.getResult().exists()) {
				snackbarHelper.show("Data pengguna tidak ditemukan",
					Snackbar.LENGTH_INDEFINITE);
				return;
			}

			mUserItem = task.getResult().toObject(UserItem.class);
			if (mUserItem == null) return;

			dismissProgress();
			binding.setUserItem(mUserItem);
		});
	}

	private void checkPermission() {
		if (isPermissionGranted()) {
			openFileChooser();
			return;
		}

		String permissionStr = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest
			.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
		permissionLauncher.launch(permissionStr);
	}

	private void openFileChooser() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		pickPhotoLauncher
			.launch(Intent.createChooser(intent, "Pilih foto profil akun"));
	}

	public void uploadFile() {
		if (imageUri == null) return;

		showProgress();
		final StorageReference fileReference = storageReference.child
			(firebaseUser.getUid() + "."
				+ getFileExtension(imageUri));

		fileReference.putFile(imageUri)
			.addOnCompleteListener(taskPutFile -> {
				dismissProgress();
				if (!taskPutFile.isSuccessful()) {
					showError(taskPutFile.getException());
					return;
				}

				showProgress();
				currentUserRef.update("photo", fileReference.getName())
					.addOnCompleteListener(taskUpdateData -> {
						dismissProgress();
						if (!taskUpdateData.isSuccessful()) {
							showError(taskUpdateData.getException());
							return;
						}
						initViews();
					});
			});
	}

	private void deletePhoto() {
		if (mUserItem.getPhoto() == null) return;
		showProgress();
		firebaseStorage.getReference(getString(R.string.collection_user))
			.child(mUserItem.getPhoto()).delete()
			.addOnCompleteListener(task -> {
				dismissProgress();
				if (!task.isSuccessful()) {
					showError(task.getException());
					return;
				}
				deleteUrlPhoto();
			});
	}

	private void deleteUrlPhoto() {
		showProgress();
		currentUserRef.update("photo", null)
			.addOnCompleteListener(t -> {
				dismissProgress();
				if (!t.isSuccessful()) {
					showError(t.getException());
					return;
				}
				initViews();
			});
	}

	private void setLogOut() {
		if (currentUserRef == null) return;

		showProgress();
		currentUserRef.update("uidDevice", null)
			.addOnCompleteListener(task -> {
				dismissProgress();
				if (!task.isSuccessful()) {
					showError(task.getException());
					return;
				}
				firebaseAuth.signOut();
			});
	}

	private String getFileExtension(Uri uri) {
		ContentResolver cR = requireActivity().getContentResolver();
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		return mime.getExtensionFromMimeType(cR.getType(uri));
	}

	private void showProgress() {
		binding.progressbar.setVisibility(View.VISIBLE);
		binding.layoutMain.setVisibility(View.GONE);
		mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("SetTextI18n")
	private void dismissProgress() {
		binding.progressbar.setVisibility(View.GONE);
		binding.layoutMain.setAnimation(animation);
		binding.layoutMain.setVisibility(View.VISIBLE);
		mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	@SuppressLint("InlinedApi")
	private boolean isPermissionGranted() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			return ActivityCompat.checkSelfPermission(mContext, Manifest.permission
				.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
		} else {
			return ActivityCompat.checkSelfPermission(mContext, Manifest.permission
				.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
		}
	}

	ActivityResultLauncher<Intent> pickPhotoLauncher = registerForActivityResult(
		new ActivityResultContracts.StartActivityForResult(),
		result -> {
			if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null &&
				result.getData().getData() != null) {
				imageUri = result.getData().getData();
				uploadFile();
			}
		}
	);
	ActivityResultLauncher<String> permissionLauncher =
		registerForActivityResult(
			new ActivityResultContracts.RequestPermission(), isGranted -> {
				if (isGranted) {
					openFileChooser();
					return;
				}
				snackbarHelper.show("Izinkan akses!", Snackbar.LENGTH_INDEFINITE);
			});
}