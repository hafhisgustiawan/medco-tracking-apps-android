package com.medco.trackingapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.medco.trackingapp.activity.ManageWellActivity;
import com.medco.trackingapp.adapter.WellAdapter;
import com.medco.trackingapp.databinding.FragmentWellBinding;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.WellItem;
import com.medco.trackingapp.model.viewmodel.StringViewModel;

import java.util.Objects;

public class WellFragment extends BaseFragment {
	public static final String TAG = "WellFragment";
	private Context mContext;
	private FragmentWellBinding binding;
	private SnackbarHelper snackbarHelper;
	//	private Animation animation;
	private CollectionReference wellColl;
	private DocumentReference currentUserRef;
	private WellAdapter adapter;
	private StringViewModel mViewModel;
//	private String catStr;

	public WellFragment() {
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
		binding = FragmentWellBinding.inflate(inflater, container, false);
//		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
		snackbarHelper = new SnackbarHelper(requireActivity().findViewById(android.R.id
			.content), binding.anchorPoint);

		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		wellColl = firebaseFirestore.collection(getString(R.string.collection_well));

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
			binding.tvFilter.setText(Objects.equals(s, "") ? "Semua" : Objects
				.equals(s, "as") ? "AS (Alur Siwah)" : Objects
				.equals(s, "jr") ? "JR (Julok Rayeuk)" : "Other Well (Sumur Tua)");
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
		binding.btnAdd.setOnClickListener(view -> startActivity(new Intent(mContext,
			ManageWellActivity.class)));

		binding.tvFilter.setOnClickListener(view -> {
			PopupMenu popupMenu = new PopupMenu(mContext, binding.tvFilter);
			popupMenu.inflate(R.menu.filter_well_menu);
			popupMenu.setOnMenuItemClickListener(item -> {
				switch (item.getItemId()) {
					case R.id.action_all:
						mViewModel.setStringState("");
						break;
					case R.id.action_as:
						mViewModel.setStringState("as");
						break;
					case R.id.action_jr:
						mViewModel.setStringState("jr");
						break;
					case R.id.action_other_well:
						mViewModel.setStringState("otherwell");
						break;
				}
				return false;
			});
			popupMenu.show();
		});
	}

	private void initRecyclerView() {
//		showProgress();

		PagedList.Config config = new PagedList.Config.Builder()
			.setInitialLoadSizeHint(1)
			.setPageSize(100)
			.build();

		FirestorePagingOptions<WellItem> options = new FirestorePagingOptions.Builder<WellItem>()
			.setLifecycleOwner(this)
			.setQuery(getQuery(), config, WellItem.class).build();

		adapter = new WellAdapter(options, mContext, "vertical");
		binding.rvWell.setLayoutManager(new LinearLayoutManager(mContext));
		binding.rvWell.setAdapter(adapter);

		adapter.setOnStateChangeListener(e -> {
			if (e != null) showError(e);
//			if (binding.progressbar.getVisibility() == View.VISIBLE) dismissProgress();

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
			return wellColl.whereEqualTo("category", catStr).orderBy("createdAt",
				Query.Direction.DESCENDING);
		}
		return wellColl.orderBy("createdAt", Query.Direction.DESCENDING);
	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	/*private void showProgress() {
		binding.progressbar.setVisibility(View.VISIBLE);
		binding.rvWell.setVisibility(View.INVISIBLE);
		binding.tvNotFound.setVisibility(View.GONE);
	}

	private void dismissProgress() {
		binding.progressbar.setVisibility(View.GONE);
		binding.rvWell.setAnimation(animation);
		binding.rvWell.setVisibility(View.VISIBLE);
	}*/
}