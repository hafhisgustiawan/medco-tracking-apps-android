package com.medco.trackingapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.medco.trackingapp.databinding.FragmentAboutBinding;

public class AboutFragment extends BottomSheetDialogFragment {

	public static final String TAG = "About Fragment";
	public ListenerClose listenerClose;

	public AboutFragment() {
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
		FragmentAboutBinding binding = FragmentAboutBinding.inflate(inflater, container, false);

		binding.tvClose.setOnClickListener(view -> {
			if (listenerClose != null) listenerClose.listenerCLose();
		});

		return binding.getRoot();
	}

	public interface ListenerClose {
		void listenerCLose();
	}
}