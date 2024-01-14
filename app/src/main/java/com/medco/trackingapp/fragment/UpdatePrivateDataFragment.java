package com.medco.trackingapp.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.FragmentUpdatePrivateDataBinding;
import com.medco.trackingapp.helper.AuthErrorHelper;
import com.medco.trackingapp.helper.EtWatcher;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.PrivateDataItem;
import com.medco.trackingapp.model.UserItem;
import com.medco.trackingapp.model.viewmodel.PrivateDataViewModel;

import java.util.Objects;

public class UpdatePrivateDataFragment extends BottomSheetDialogFragment {
	public static final String TAG = "UpdatePrivateDataFragment";
	// 1) constructor variable
	public UserItem mUserItem;
	public int mSelector; //0 -> name || 1 -> email || 2 -> phone
	// 3) callback
	public ListenerClose listenerClose;
	private Context mContext;
	private FragmentActivity mActivity;
	private FragmentUpdatePrivateDataBinding binding;
	private SnackbarHelper snackbarHelper;
	// 2) firebase
	private FirebaseAuth firebaseAuth;
	private FirebaseUser firebaseUser;
	private DocumentReference currentUserRef;
	// 3) view model
	private PrivateDataViewModel mViewModel;

	public UpdatePrivateDataFragment(UserItem userItem, int selector) {
		// Required empty public constructor
		this.mUserItem = userItem;
		this.mSelector = selector;
	}

	public void ListenerApiClose(ListenerClose listener) {
		listenerClose = listener;
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
		binding = FragmentUpdatePrivateDataBinding.inflate(inflater, container,
			false);
		snackbarHelper = new SnackbarHelper(Objects.requireNonNull(requireDialog().getWindow())
			.getDecorView(), binding.spacer);

		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser == null) return binding.getRoot();

		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		currentUserRef = firebaseFirestore.collection(getString(R.string.collection_user))
			.document(firebaseUser.getUid());

		mViewModel = new ViewModelProvider(requireActivity()).get(PrivateDataViewModel.class);

		initViews();
		initListeners();
		return binding.getRoot();
	}

	public void initViews() {
		if (firebaseUser == null) listenerClose.listenerClose(0);
		binding.setUserItem(mUserItem);

		PrivateDataItem currentItem = new PrivateDataItem();
		currentItem.setType(mSelector == 0 ? "name" : mSelector == 1 ? "email" : "phone");
		mViewModel.setPrivateData(currentItem);

		mViewModel.getPrivateData().observe(this, privateDataItem -> binding
			.setPrivateDataItem(privateDataItem));
	}

	public void initListeners() {
		binding.tvClose.setOnClickListener(view1 -> listenerClose.listenerClose(0));

		binding.etDataNew.addTextChangedListener(new EtWatcher(str -> {
			PrivateDataItem item = mViewModel.getPrivateData().getValue();
			if (item == null) return;
			item.setNewData(str);
			mViewModel.setPrivateData(item);
		}));

		binding.etPassword.addTextChangedListener(new EtWatcher(str -> {
			PrivateDataItem item = mViewModel.getPrivateData().getValue();
			if (item == null) return;
			item.setPassword(str.trim());
			mViewModel.setPrivateData(item);
		}));

		binding.btnSave.setOnClickListener(v -> {
			String newData = binding.etDataNew.getText() != null ? binding.etDataNew.getText()
				.toString().trim() : null;
			String password = binding.etPassword.getText() != null ? binding.etPassword.getText()
				.toString().trim() : null;

			new AlertDialog.Builder(mContext)
				.setCancelable(true)
				.setMessage("Anda yakin data sudah benar?")
				.setNegativeButton("Tidak", null)
				.setPositiveButton("Ya", (dialogInterface, i) -> {
					if (mSelector == 1) { //email update
						checkLogin(newData, firebaseUser.getEmail(), password);
					} else if (mSelector == 2) { //phone update
						updatePhone(newData);
					} else { //name update
						updateName(newData);
					}
				}).create().show();
		});
	}

	private void updatePhone(String newPhone) {
		showProgress();
		currentUserRef.update("phone", newPhone)
			.addOnCompleteListener(task -> {
				dismissProgress();
				if (!task.isSuccessful()) {
					showError(task.getException());
					return;
				}
				listenerClose.listenerClose(1);
			});
	}

	private void updateName(String newName) {
		showProgress();
		currentUserRef.update("name", newName)
			.addOnCompleteListener(task -> {
				dismissProgress();
				if (!task.isSuccessful()) {
					showError(task.getException());
					return;
				}
				listenerClose.listenerClose(1);
			});
	}

	private void checkLogin(String newEmail, String oldEmail, String password) {
		showProgress();
		firebaseAuth.signInWithEmailAndPassword(oldEmail, password)
			.addOnCompleteListener(task -> {
				dismissProgress();
				if (task.isSuccessful()) {
					if (task.getResult().getUser() != null) updateEmail(newEmail);
					return;
				}

				if (!(task.getException() instanceof FirebaseAuthException)) {
					showError(task.getException());
					return;
				}

				Log.e(TAG, "loginWithEmailAndPassword: ", task.getException());
				String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

				int isAuthErrorCritical = AuthErrorHelper.isAuthErrorCritical(errorCode);
				String errorDesc = AuthErrorHelper.getAuthErrorDesc(errorCode);

				if (isAuthErrorCritical == 0) {
					snackbarHelper.show(errorDesc, Snackbar.LENGTH_INDEFINITE);
					return;
				}

				if (isAuthErrorCritical == 1) {
					binding.tilDataNew.setError(errorDesc);
					binding.tilDataNew.requestFocus();
					return;
				}

				binding.tilPassword.setError(errorDesc);
				binding.tilPassword.requestFocus();
			});
	}

	private void updateEmail(String newEmail) {
		showProgress();
		firebaseUser.updateEmail(newEmail)
			.addOnCompleteListener(task -> {
				dismissProgress();
				if (!task.isSuccessful()) {
					showError(task.getException());
					return;
				}
				listenerClose.listenerClose(1);
			});
	}

	private void showProgress() {
		binding.setLoading(false);
		mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("SetTextI18n")
	private void dismissProgress() {
		binding.setLoading(false);
		mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	public interface ListenerClose {
		void listenerClose(int selector);
	}
}