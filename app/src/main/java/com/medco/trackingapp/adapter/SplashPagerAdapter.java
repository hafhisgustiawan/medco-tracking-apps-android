package com.medco.trackingapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.medco.trackingapp.fragment.SplashFragment;

public class SplashPagerAdapter extends FragmentStateAdapter {

	public SplashPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
		super(fragmentManager, lifecycle);
	}

	@NonNull
	@Override
	public Fragment createFragment(int position) {
		return new SplashFragment(position);
	}

	@Override
	public int getItemCount() {
		return 3;
	}
}
