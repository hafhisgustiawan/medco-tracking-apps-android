package com.medco.trackingapp.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.medco.trackingapp.databinding.ActivityResetPasswordBinding;
import com.medco.trackingapp.helper.EtWatcher;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.viewmodel.StringViewModel;

import java.util.List;

public class ResetPasswordActivity extends BaseActivity {

	public static final String TAG = "ResetPasswordActivity";
	private ActivityResetPasswordBinding binding;
	private SnackbarHelper snackbarHelper;
	private FirebaseAuth firebaseAuth;
	private StringViewModel mViewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		snackbarHelper = new SnackbarHelper(findViewById(android.R.id.content), null);

		firebaseAuth = FirebaseAuth.getInstance();

		mViewModel = new ViewModelProvider(this).get(StringViewModel.class);

		initViews();
		initListeners();
	}

	@Override
	public void initViews() {
		binding.setEmail(mViewModel.getStringState().getValue());
		mViewModel.getStringState().observe(this, s -> binding.setEmail(s));
	}

	@Override
	public void initListeners() {
		binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

		binding.etEmail.addTextChangedListener(new EtWatcher(str -> mViewModel
			.setStringState(str)));

		binding.btnNext.setOnClickListener(view -> {
			String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString()
				.trim() : null;

			if (email == null) return;

			showProgress();
			firebaseAuth.fetchSignInMethodsForEmail(email)
				.addOnCompleteListener(task -> {
					dismissProgress();
					if (!task.isSuccessful()) {
						showError(task.getException());
						return;
					}

					List<String> methods = task.getResult().getSignInMethods();
					if (methods == null || methods.isEmpty()) {
						snackbarHelper.show("Email Anda belum terdaftar di sistem aplikasi",
							Snackbar.LENGTH_INDEFINITE);
						return;
					}

					//berhasil
					showProgress();
					firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(t -> {
						dismissProgress();
						if (!task.isSuccessful()) {
							showError(task.getException());
							return;
						}
						snackbarHelper.show("Silahkan pesan yang kami kirimkan ke email Anda",
							Snackbar.LENGTH_INDEFINITE);
					});
				});
		});
	}

	private void showProgress() {
		binding.setLoading(true);
		binding.btnNext.setEnabled(false);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("SetTextI18n")
	private void dismissProgress() {
		binding.setLoading(false);
		binding.btnNext.setEnabled(true);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}
}