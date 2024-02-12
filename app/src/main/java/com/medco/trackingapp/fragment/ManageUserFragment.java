package com.medco.trackingapp.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.FragmentManageUserBinding;
import com.medco.trackingapp.helper.EtWatcher;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.ManageUserItem;
import com.medco.trackingapp.model.UserItem;
import com.medco.trackingapp.model.viewmodel.ManageUserViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManageUserFragment extends BottomSheetDialogFragment {

	public static final String TAG = ManageUserFragment.class.getSimpleName();
	private final DocumentSnapshot mUserSnapshot;
	private Context mContext;
	private SnackbarHelper snackbarHelper;
	private FragmentManageUserBinding binding;
	private CollectionReference userColl;
	private ManageUserViewModel mViewModel;

	//callback
	public ListenerClose listenerClose;

	public ManageUserFragment(DocumentSnapshot userSnapshot) {
		// Required empty public constructor
		this.mUserSnapshot = userSnapshot;
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

		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		userColl = firebaseFirestore.collection(getString(R.string.collection_user));

		mViewModel = new ViewModelProvider(this).get(ManageUserViewModel.class);

		initViews();
		initListeners();
		return binding.getRoot();
	}

	public void initViews() {
		mViewModel.getManageUserState().observe(this, manageUserItem -> binding
			.setManageUserItem(manageUserItem));

		if (mUserSnapshot == null) return;
		UserItem userItem = mUserSnapshot.toObject(UserItem.class);
		binding.setUserItem(userItem);
	}

	public void initListeners() {
		binding.tvClose.setOnClickListener(view -> {
			if (listenerClose == null) return;
			listenerClose.listenerClose(0);
		});

		binding.etName.addTextChangedListener(new EtWatcher(str -> {
			ManageUserItem item = mViewModel.getManageUserState().getValue();
			if (item == null) return;
			item.setName(str);
			mViewModel.setManageUserState(item);
		}));

		binding.etEmail.addTextChangedListener(new EtWatcher(str -> {
			ManageUserItem item = mViewModel.getManageUserState().getValue();
			if (item == null) return;
			item.setEmail(str.trim());
			mViewModel.setManageUserState(item);
		}));

		binding.etPhone.addTextChangedListener(new EtWatcher(str -> {
			ManageUserItem item = mViewModel.getManageUserState().getValue();
			if (item == null) return;
			item.setPhone(str.trim());
			mViewModel.setManageUserState(item);
		}));

		binding.btnSave.setOnClickListener(view -> {
			String name = binding.etName.getText() != null ? binding.etName.getText()
				.toString().trim() : null;
			String email = binding.etEmail.getText() != null ? binding.etEmail.getText()
				.toString().trim() : null;
			String phone = binding.etPhone.getText() != null ? binding.etPhone.getText()
				.toString().trim() : null;

			new AlertDialog.Builder(mContext)
				.setCancelable(true)
				.setMessage("Anda yakin data sudah benar?")
				.setNegativeButton("Tidak", null)
				.setPositiveButton("Ya", (dialogInterface, i) -> {
					if (mUserSnapshot == null) {
						addUser(name, email, phone);
					} else {
						updateUser(name, email, phone);
					}
				}).create().show();
		});
	}

	private void addUser(String name, String email, String phone) {
		UserItem userItem = new UserItem();
		userItem.setName(name);
		userItem.setEmail(email);
		userItem.setPhone(phone);
		userItem.setRole("user");
		userItem.setTimeRegister(Timestamp.now());
		userItem.setUidDevice(null);
		userItem.setPhoto(null);

		showProgress();
		userColl.whereEqualTo("email", email).get().addOnCompleteListener(task -> {
			dismissProgress();
			if (!task.isSuccessful()) {
				showError(task.getException());
				return;
			}

			if (!task.getResult().isEmpty()) {
				snackbarHelper.show("Email tidak bisa duplikat", Snackbar.LENGTH_INDEFINITE);
				return;
			}

			showProgress();
			userColl.add(userItem).addOnCompleteListener(t -> {
				dismissProgress();
				if (!t.isSuccessful()) {
					showError(t.getException());
					return;
				}
				if (listenerClose == null) return;
				listenerClose.listenerClose(1);
			});
		});
	}

	private void updateUser(String name, String email, String phone) {
		List<Task<Void>> tasks = new ArrayList<>();
		tasks.add(mUserSnapshot.getReference().update("name", name));
//		tasks.add(mUserSnapshot.getReference().update("email", email));
		tasks.add(mUserSnapshot.getReference().update("phone", phone));

		showProgress();
		Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
			dismissProgress();
			if (!task.isSuccessful()) {
				showError(task.getException());
				return;
			}

			if (listenerClose == null) return;
			listenerClose.listenerClose(1);
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