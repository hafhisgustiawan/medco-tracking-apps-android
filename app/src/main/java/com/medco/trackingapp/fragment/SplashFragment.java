package com.medco.trackingapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.FragmentSplashBinding;

public class SplashFragment extends Fragment {

	public static final String TAG = "SplashFragment";

	//constructor variable
	private final int mSelector;

	public SplashFragment(int selector) {
		// Required empty public constructor
		this.mSelector = selector;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@SuppressLint("UseCompatLoadingForDrawables")
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState) {
		Context mContext = requireContext();
		FragmentSplashBinding binding = FragmentSplashBinding.inflate(inflater, container,
			false);

		int imgId;
		int titleId;
		int subTitleId;

		if (mSelector == 1) {
			imgId = R.drawable.grid_1;
			titleId = R.string.title_splash_2;
			subTitleId = R.string.sub_title_splash_2;
		} else if (mSelector == 2) {
			imgId = R.drawable.grid_2;
			titleId = R.string.title_splash_3;
			subTitleId = R.string.sub_title_splash_3;
		} else {
			imgId = R.drawable.grid_3;
			titleId = R.string.title_splash_1;
			subTitleId = R.string.sub_title_splash_1;
		}

		binding.imgSplash.setImageDrawable(mContext.getDrawable(imgId));
		binding.tvTitle.setText(mContext.getString(titleId));
		binding.tvSubTitle.setText(mContext.getString(subTitleId));

		return binding.getRoot();
	}
}