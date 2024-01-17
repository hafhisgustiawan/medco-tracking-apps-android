package com.medco.trackingapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.FragmentManageReportBinding;
import com.medco.trackingapp.helper.SnackbarHelper;

public class ManageReportFragment extends BottomSheetDialogFragment {

	public static final String TAG = "ManageReportFragment";
	public DocumentSnapshot mReportSnapshot;
	private Context mContext;
	private SnackbarHelper snackbarHelper;
	private FragmentManageReportBinding binding;

	//callback
	public ListenerClose listenerClose;

	public void ListenerApiClose(ListenerClose listener) {
		listenerClose = listener;
	}

	public ManageReportFragment(DocumentSnapshot reportSnapshot) {
		// Required empty public constructor
		this.mReportSnapshot = reportSnapshot;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_manage_report, container, false);
	}

	public void initViews() {

	}

	public void initListeners() {

	}


	public interface ListenerClose {
		void listenerClose(int selector); // 0 -> close dan 1 > reload parent
	}
}