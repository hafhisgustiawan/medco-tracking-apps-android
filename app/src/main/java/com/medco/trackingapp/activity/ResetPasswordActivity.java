package com.medco.trackingapp.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.medco.trackingapp.databinding.ActivityResetPasswordBinding;
import com.medco.trackingapp.helper.SnackbarHelper;

public class ResetPasswordActivity extends BaseActivity {

	public static final String TAG = "ResetPasswordActivity";
	private ActivityResetPasswordBinding binding;
	private SnackbarHelper snackbarHelper;
	private FirebaseAuth firebaseAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		snackbarHelper = new SnackbarHelper(findViewById(android.R.id.content), null);

		firebaseAuth = FirebaseAuth.getInstance();

		initViews();
		initListeners();
	}

	@Override
	public void initViews() {

	}

	@Override
	public void initListeners() {
		binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
	}
}