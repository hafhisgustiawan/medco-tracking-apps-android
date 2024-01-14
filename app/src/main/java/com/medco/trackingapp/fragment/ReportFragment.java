package com.medco.trackingapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.medco.trackingapp.databinding.FragmentReportBinding;
import com.medco.trackingapp.helper.SnackbarHelper;

public class ReportFragment extends BaseFragment {

	public static final String TAG = "ReportFragment";
	private Context mContext;
	private FragmentReportBinding binding;
	private SnackbarHelper snackbarHelper;
	private DocumentReference currentUserRef;

	public ReportFragment() {
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
		binding = FragmentReportBinding.inflate(inflater, container, false);

		return binding.getRoot();
	}

	@Override
	public void initViews() {

	}

	@Override
	public void initListeners() {

	}
}