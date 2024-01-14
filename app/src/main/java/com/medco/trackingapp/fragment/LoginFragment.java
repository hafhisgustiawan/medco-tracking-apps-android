package com.medco.trackingapp.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.FragmentLoginBinding;
import com.medco.trackingapp.helper.AuthErrorHelper;
import com.medco.trackingapp.helper.EtWatcher;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.LoginItem;
import com.medco.trackingapp.model.UserItem;
import com.medco.trackingapp.model.viewmodel.LoginViewModel;

import java.util.Objects;

public class LoginFragment extends BottomSheetDialogFragment {

	public static final String TAG = "LoginFragment";
	//callback
	public ListenerClose listenerClose;
	private FragmentLoginBinding binding;
	private SnackbarHelper snackbarHelper;
	//firebase
	private FirebaseAuth firebaseAuth;
	private CollectionReference userColl;
	//view model
	private LoginViewModel mViewModel;

	public LoginFragment() {
		// Required empty public constructor
	}

	public void ListenerApiClose(ListenerClose listener) {
		this.listenerClose = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState) {
		binding = FragmentLoginBinding.inflate(inflater, container, false);
		snackbarHelper = new SnackbarHelper(Objects.requireNonNull(requireDialog()
			.getWindow()).getDecorView(), binding.spacer);

		firebaseAuth = FirebaseAuth.getInstance();
		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		userColl = firebaseFirestore.collection(getString(R.string.collection_user));

		mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

		initViews();
		initListeners();
		return binding.getRoot();
	}

	public void initViews() {
		mViewModel.getLoginState().observe(this, loginItem -> binding
			.setLoginItem(loginItem));
	}

	public void initListeners() {
		binding.tvClose.setOnClickListener(view -> listenerClose.listenerClose
			(null));

		binding.etEmail.addTextChangedListener(new EtWatcher(str -> {
			LoginItem item = mViewModel.getLoginState().getValue();
			if (item == null) return;
			item.setEmail(str.trim());
			mViewModel.setLoginState(item);
		}));

		binding.etPassword.addTextChangedListener(new EtWatcher(str -> {
			LoginItem item = mViewModel.getLoginState().getValue();
			if (item == null) return;
			item.setPassword(str.trim());
			mViewModel.setLoginState(item);
		}));

		binding.btnLogIn.setOnClickListener(view1 -> {
			String email = binding.etEmail.getText() != null ? binding.etEmail.getText()
				.toString().trim() : null;
			String password = binding.etPassword.getText() != null ? binding.etPassword.getText()
				.toString().trim() : null;

			loginWithEmailAndPassword(email, password);
		});
	}

	private void loginWithEmailAndPassword(String email, String password) {
		showProgress();
		firebaseAuth.signInWithEmailAndPassword(email, password)
			.addOnCompleteListener(task -> {
				if (task.isSuccessful()) {
					if (task.getResult().getUser() != null) doLogin(task.getResult().getUser());
					return;
				}

				dismissProgress();
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
					binding.tilEmail.setError(errorDesc);
					binding.tilEmail.requestFocus();
					return;
				}

				binding.tilPassword.setError(errorDesc);
				binding.tilPassword.requestFocus();
			});
	}

	private void doLogin(@NonNull FirebaseUser user) {
		userColl
			.document(user.getUid())
			.get()
			.addOnCompleteListener(task -> {
				dismissProgress();
				if (!task.isSuccessful()) {
					firebaseAuth.signOut();
					showError(task.getException());
					return;
				}

				if (!task.getResult().exists()) {
					firebaseAuth.signOut();
					binding.tilEmail.setError("Alamat email belum terdaftar, silahkan registrasi");
					return;
				}

				UserItem userItem = task.getResult().toObject(UserItem.class);
				if (userItem == null) {
					firebaseAuth.signOut();
					return;
				}
				listenerClose.listenerClose(user);
			});
	}

	private void showProgress() {
		binding.setLoading(true);
		binding.btnLogIn.setEnabled(false);
		Objects.requireNonNull(requireDialog().getWindow()).setFlags(WindowManager
			.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("SetTextI18n")
	private void dismissProgress() {
		binding.setLoading(false);
		binding.btnLogIn.setEnabled(true);
		Objects.requireNonNull(requireDialog().getWindow()).clearFlags(WindowManager
			.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	public interface ListenerClose {
		void listenerClose(FirebaseUser firebaseUser);
	}
}