package com.medco.trackingapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.medco.trackingapp.adapter.SplashPagerAdapter;
import com.medco.trackingapp.databinding.ActivityLandingBinding;

public class LandingActivity extends BaseActivity {

	public static final String TAG = LandingActivity.class.getSimpleName();
	private Context mContext;
	private Boolean isSplashScreen;
	private ActivityLandingBinding binding;
	private FirebaseUser firebaseUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

		super.onCreate(savedInstanceState);
		mContext = this;
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		binding = ActivityLandingBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		isSplashScreen = true;
		splashScreen.setKeepOnScreenCondition(() -> isSplashScreen);

		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();

		new Handler().postDelayed(() -> {
			isSplashScreen = false;
			initViews();
			initListeners();
		}, 2000);
	}

	@Override
	public void initViews() {
		if (firebaseUser != null) {
			goToDestination();
			return;
		}

		binding.btnPrevious.setVisibility(View.INVISIBLE);
		binding.btnPrevious.setEnabled(false);

		SplashPagerAdapter pagerAdapter = new SplashPagerAdapter(getSupportFragmentManager(),
			getLifecycle());
		binding.viewPager.setAdapter(pagerAdapter);
		new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
		})
			.attach();
	}

	@Override
	public void initListeners() {
		binding.tabLayout.addOnTabSelectedListener(new TabLayout
			.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				binding.btnLogin.setVisibility(tab.getPosition() == 2 ? View
					.VISIBLE : View.GONE);
				binding.btnNext.setVisibility(tab.getPosition() == 2 ? View
					.INVISIBLE : View.VISIBLE);
				binding.btnNext.setEnabled(tab.getPosition() != 2);
				binding.btnPrevious.setVisibility(tab.getPosition() == 0 ? View
					.INVISIBLE : View.VISIBLE);
				binding.btnPrevious.setEnabled(tab.getPosition() != 0);
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

		binding.btnNext.setOnClickListener(view -> {
			TabLayout.Tab tab = binding.tabLayout.getTabAt(binding.tabLayout
				.getSelectedTabPosition() + 1);
			if (tab != null) tab.select();
		});

		binding.btnPrevious.setOnClickListener(view -> {
			TabLayout.Tab tab = binding.tabLayout.getTabAt(binding.tabLayout
				.getSelectedTabPosition() - 1);
			if (tab != null) tab.select();
		});

		binding.btnLogin.setOnClickListener(view -> {
			startActivity(new Intent(this, AuthActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
			finish();
		});

		binding.tvSkip.setOnClickListener(view -> {
			startActivity(new Intent(this, AuthActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
			finish();
		});
	}

	private void goToDestination() {
		Intent intent = new Intent(mContext, MainActivity.class);
		startActivity(intent
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
		finish();
	}
}