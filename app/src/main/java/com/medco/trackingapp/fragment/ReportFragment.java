package com.medco.trackingapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.medco.trackingapp.R;
import com.medco.trackingapp.adapter.ReportAdapter;
import com.medco.trackingapp.databinding.FragmentReportBinding;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.ReportItem;
import com.medco.trackingapp.model.viewmodel.StringViewModel;

import java.util.Objects;

public class ReportFragment extends BaseFragment {

	public static final String TAG = "ReportFragment";
	private Context mContext;
	private FragmentReportBinding binding;
	private SnackbarHelper snackbarHelper;
	private Animation animation;
	private DocumentReference currentUserRef;
	private CollectionReference reportColl;
	private ReportAdapter adapter;
	private StringViewModel mViewModel;

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
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
		snackbarHelper = new SnackbarHelper(requireActivity().findViewById(android.R.id
			.content), binding.anchorPoint);

		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		reportColl = firebaseFirestore.collection(getString(R.string.collection_report));

		if (firebaseUser == null) return binding.getRoot();
		currentUserRef = firebaseFirestore.collection(getString(R.string.collection_user))
			.document(firebaseUser.getUid());


		mViewModel = new ViewModelProvider(this).get(StringViewModel.class);

		initViews();
		initListeners();
		return binding.getRoot();
	}

	@Override
	public void initViews() {
		binding.setUserRef(currentUserRef);

		mViewModel.getStringState().observe(getViewLifecycleOwner(), s -> {
			binding.tvFilter.setText(Objects.equals(s, "") ? "Semua" : "Milik saya");
			initRecyclerView();
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (adapter == null) {
			initRecyclerView();
		} else {
			adapter.refresh();
		}
	}

	@SuppressLint("NonConstantResourceId")
	@Override
	public void initListeners() {
		binding.tvFilter.setOnClickListener(view -> {
			PopupMenu popupMenu = new PopupMenu(mContext, binding.tvFilter);
			popupMenu.inflate(R.menu.filter_report_menu);
			popupMenu.setOnMenuItemClickListener(item -> {
				switch (item.getItemId()) {
					case R.id.action_all:
						mViewModel.setStringState("");
						break;
					case R.id.action_mine:
						mViewModel.setStringState("mine");
						break;
				}
				return false;
			});
			popupMenu.show();
		});
	}

	private void initRecyclerView() {
		showProgress();

		PagedList.Config config = new PagedList.Config.Builder()
			.setInitialLoadSizeHint(1)
			.setPageSize(100)
			.build();

		FirestorePagingOptions<ReportItem> options = new FirestorePagingOptions.Builder
			<ReportItem>().setLifecycleOwner(this).setQuery(getQuery(), config, ReportItem
			.class).build();

		adapter = new ReportAdapter(options, mContext);
		binding.rvReport.setLayoutManager(new LinearLayoutManager(mContext));
		binding.rvReport.setAdapter(adapter);

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

	private Query getQuery() {
		String catStr = mViewModel.getStringState().getValue();
		if (!Objects.equals(catStr, "")) {
			return reportColl.whereEqualTo("userRef", currentUserRef).orderBy("createdAt",
				Query.Direction.DESCENDING);
		}
		return reportColl.orderBy("createdAt", Query.Direction.DESCENDING);
	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	private void showProgress() {
		binding.progressbar.setVisibility(View.VISIBLE);
		binding.rvReport.setVisibility(View.INVISIBLE);
		binding.tvNotFound.setVisibility(View.GONE);
	}

	private void dismissProgress() {
		binding.progressbar.setVisibility(View.GONE);
		binding.rvReport.setAnimation(animation);
		binding.rvReport.setVisibility(View.VISIBLE);
	}
}