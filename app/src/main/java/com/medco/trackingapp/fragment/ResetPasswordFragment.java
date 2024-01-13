package com.medco.trackingapp.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medco.trackingapp.databinding.FragmentResetPasswordBinding;
import com.medco.trackingapp.helper.SnackbarHelper;

import java.util.Objects;

public class ResetPasswordFragment extends BottomSheetDialogFragment {

	public static final String TAG = "ResetPasswordFragment";
	//callback
	public ListenerClose listenerClose;
	private FragmentActivity mActivity;
	private FragmentResetPasswordBinding binding;
	private SnackbarHelper snackbarHelper;
	private FirebaseAuth firebaseAuth;
	private FirebaseUser firebaseUser;

	public ResetPasswordFragment() {
		// Required empty public constructor
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
		binding = FragmentResetPasswordBinding.inflate(inflater, container, false);
		mActivity = requireActivity();
		snackbarHelper = new SnackbarHelper(Objects.requireNonNull(requireDialog().getWindow())
			.getDecorView(), binding.spacer);

		//firebase
		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();

		initViews();
		initListeners();
		return binding.getRoot();
	}

	@SuppressLint("SetTextI18n")
	public void initViews() {
		if (firebaseUser == null) listenerClose.listenerClose();
		binding.tvLabel.setText("Instruksi untuk mengatur ulang kata sandi akan dikirimkan " +
			"ke email " + firebaseUser.getEmail());
	}

	public void initListeners() {
		binding.tvClose.setOnClickListener(view -> listenerClose.listenerClose());

		binding.btnNext.setOnClickListener(view -> {
			if (firebaseUser.getEmail() == null) return;
			showProgress();
			firebaseAuth.sendPasswordResetEmail(firebaseUser.getEmail())
				.addOnCompleteListener(task -> {
					dismissProgress();
					if (!task.isSuccessful()) {
						showError(task.getException());
						return;
					}
					snackbarHelper.show("Email terkirim!", Snackbar.LENGTH_INDEFINITE);
				});

		});
	}

	private void showProgress() {
		binding.progressbar.setVisibility(View.VISIBLE);
		binding.btnNext.setVisibility(View.INVISIBLE);
		mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("SetTextI18n")
	private void dismissProgress() {
		binding.progressbar.setVisibility(View.GONE);
		binding.btnNext.setVisibility(View.VISIBLE);
		mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	public interface ListenerClose {
		void listenerClose();
	}
}