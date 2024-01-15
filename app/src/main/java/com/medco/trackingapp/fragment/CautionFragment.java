package com.medco.trackingapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.medco.trackingapp.databinding.FragmentCautionBinding;

public class CautionFragment extends BottomSheetDialogFragment {

	public static final String TAG = "Caution Fragment";

	//constructor variable
	public int mRawId;
	public String mMessage;

	//callback
	public ListenerClose listenerClose;

	public interface ListenerClose {
		void listenerCLose();
	}

	public void ListenerApiClose(ListenerClose listener) {
		listenerClose = listener;
	}

	public CautionFragment(int rawId, String message) {
		this.mRawId = rawId;
		this.mMessage = message;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState) {
		FragmentCautionBinding binding = FragmentCautionBinding.inflate(inflater, container,
			false);

		binding.animation.setAnimation(mRawId);
		binding.animation.playAnimation();
		binding.tvCaution.setText(mMessage);

		binding.btnBack.setOnClickListener(view -> listenerClose.listenerCLose());

		return binding.getRoot();
	}
}