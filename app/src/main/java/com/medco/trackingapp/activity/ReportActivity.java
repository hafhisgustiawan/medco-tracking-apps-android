package com.medco.trackingapp.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;

import com.medco.trackingapp.databinding.ActivityReportBinding;

public class ReportActivity extends BaseActivity {

	public static final String TAG = "ReportActivity";
	private Context mContext;
	private ActivityReportBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		mContext = this;
		binding = ActivityReportBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

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