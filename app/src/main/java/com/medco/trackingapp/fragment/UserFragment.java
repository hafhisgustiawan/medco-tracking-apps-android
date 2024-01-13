package com.medco.trackingapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.medco.trackingapp.R;
import com.medco.trackingapp.adapter.UserAdapter;
import com.medco.trackingapp.databinding.FragmentUserBinding;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.UserItem;

public class UserFragment extends BaseFragment {
	public static final String TAG = "UserFragment";
	private Context mContext;
	private FragmentUserBinding binding;
	private SnackbarHelper snackbarHelper;
	private Animation animation;
	private CollectionReference userColl;
	private UserAdapter adapter;

	public UserFragment() {
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
		binding = FragmentUserBinding.inflate(inflater, container, false);
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
		snackbarHelper = new SnackbarHelper(requireActivity().findViewById(android.R.id
			.content), binding.anchorPoint);

		FirebaseFirestore firestore = FirebaseFirestore.getInstance();
		userColl = firestore.collection(getString(R.string.collection_user));

		initViews();
		initListeners();

		return binding.getRoot();
	}

	@Override
	public void initViews() {
		initRecyclerView();
	}

	@Override
	public void initListeners() {

	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	private void initRecyclerView() {
		showProgress();

		PagedList.Config config = new PagedList.Config.Builder()
			.setInitialLoadSizeHint(1)
			.setPageSize(100)
			.build();

		Query query = userColl.whereEqualTo("role", "user").orderBy
			("timeRegister", Query.Direction.DESCENDING);

		FirestorePagingOptions<UserItem> options = new FirestorePagingOptions.Builder<UserItem>()
			.setLifecycleOwner(this)
			.setQuery(query, config, UserItem.class).build();

		adapter = new UserAdapter(options, mContext);
		binding.rvUser.setLayoutManager(new LinearLayoutManager(mContext));
		binding.rvUser.setAdapter(adapter);

		adapter.setOnItemClickListener(visibility -> binding.progressbar.setVisibility
			(visibility));
		adapter.setOnStateChangeListener(e -> {
			if (e != null) showError(e);
			if (binding.progressbar.getVisibility() == View.VISIBLE) dismissProgress();

			if (adapter.getItemCount() > 0) {
				binding.tvNotFound.setVisibility(View.GONE);
				return;
			}
			binding.tvNotFound.setVisibility(View.VISIBLE);
		});
	}

	private void showProgress() {
		binding.progressbar.setVisibility(View.VISIBLE);
		binding.rvUser.setVisibility(View.INVISIBLE);
		binding.tvNotFound.setVisibility(View.GONE);
	}

	private void dismissProgress() {
		binding.progressbar.setVisibility(View.GONE);
		binding.rvUser.setAnimation(animation);
		binding.rvUser.setVisibility(View.VISIBLE);
	}
}