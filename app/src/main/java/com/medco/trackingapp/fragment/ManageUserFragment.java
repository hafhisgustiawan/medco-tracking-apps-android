package com.medco.trackingapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.medco.trackingapp.databinding.FragmentManageUserBinding;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.UserItem;
import com.medco.trackingapp.model.viewmodel.ManageUserViewModel;

import java.util.Objects;

public class ManageUserFragment extends BottomSheetDialogFragment {

	public static final String TAG = "ManageUserFragment";
	//callback
	public ListenerClose listenerClose;
	private Context mContext;
	private SnackbarHelper snackbarHelper;
	private FragmentManageUserBinding binding;
	private DocumentSnapshot userSnapshot;
	private ManageUserViewModel mViewModel;

	public ManageUserFragment(DocumentSnapshot userSnapshot) {
		// Required empty public constructor
		this.userSnapshot = userSnapshot;
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
		mContext = requireContext();
		binding = FragmentManageUserBinding.inflate(inflater, container, false);
		snackbarHelper = new SnackbarHelper(Objects.requireNonNull(requireDialog()
			.getWindow()).getDecorView(), binding.spacer);

		mViewModel = new ViewModelProvider(this).get(ManageUserViewModel.class);

		initViews();
		initListeners();
		return binding.getRoot();
	}

	public void initViews() {
		mViewModel.getManageUserState().observe(this, manageUserItem -> binding
			.setManageUserItem(manageUserItem));

		if (userSnapshot == null) return;
		UserItem userItem = userSnapshot.toObject(UserItem.class);
		binding.setUserItem(userItem);
	}

	public void initListeners() {
		binding.tvClose.setOnClickListener(view -> {
			if (listenerClose == null) return;
			listenerClose.listenerClose(0);
		});
	}

	private void showProgress() {
		binding.setLoading(true);
		binding.btnSave.setEnabled(false);
		requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("SetTextI18n")
	private void dismissProgress() {
		binding.setLoading(false);
		binding.btnSave.setEnabled(true);
		requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	public interface ListenerClose {
		void listenerClose(int selector); // 0 -> close dan 1 > reload parent
	}


}