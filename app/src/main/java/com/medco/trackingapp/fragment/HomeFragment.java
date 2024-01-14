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
import androidx.fragment.app.FragmentActivity;
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
import com.medco.trackingapp.adapter.WellAdapter;
import com.medco.trackingapp.databinding.FragmentHomeBinding;
import com.medco.trackingapp.helper.CustomException;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.UserItem;
import com.medco.trackingapp.model.WellItem;

public class HomeFragment extends BaseFragment {

	public static final String TAG = "HomeFragment";
	//callback
	public ListenerChange listenerChange;
	private Context mContext;
	private FragmentActivity mActivity;
	private FragmentHomeBinding binding;
	private Animation animation;
	private SnackbarHelper snackbarHelper;

	//firebase
	private CollectionReference reportColl;
	private CollectionReference wellColl;
	private DocumentReference currentUserRef;

	//adapter
	private WellAdapter adapter;

	public HomeFragment() {
		// Required empty public constructor
	}

	public void ListenerApiChange(ListenerChange listener) {
		listenerChange = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState) {
		mContext = requireContext();
		mActivity = requireActivity();
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		snackbarHelper = new SnackbarHelper(mActivity.findViewById(android.R.id
			.content), binding.anchorPoint);

		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		reportColl = firebaseFirestore.collection(getString(R.string.collection_report));
		wellColl = firebaseFirestore.collection(getString(R.string.collection_well));

		if (firebaseUser == null) {
			showError(new CustomException("Data pengguna tidak ditemukan", new Throwable()));
			return binding.getRoot();
		}

		currentUserRef = firebaseFirestore.collection(getString(R.string.collection_user))
			.document(firebaseUser.getUid());

		initViews();
		initListeners();
		return binding.getRoot();
	}

	@Override
	public void initViews() {
		initUserLogin();
		initRecyclerWell();
	}

	@Override
	public void initListeners() {
		binding.btnManageAccount.setOnClickListener(view -> listenerChange
			.onListenerChange(R.id.nav_acc));
		binding.tvSeeAllWell.setOnClickListener(view -> listenerChange
			.onListenerChange(R.id.nav_well));
		binding.tvSeeAllReport.setOnClickListener(view -> listenerChange
			.onListenerChange(R.id.nav_report));
	}

	private void initUserLogin() {
		showProgress();
		currentUserRef.get().addOnCompleteListener(task -> {
			dismissProgress();
			if (!task.isSuccessful()) {
				showError(task.getException());
				return;
			}
			UserItem userItem = task.getResult().toObject(UserItem.class);
			binding.setUserItem(userItem);
		});
	}

	private void initRecyclerWell() {
		showProgress();

		PagedList.Config config = new PagedList.Config.Builder()
			.setInitialLoadSizeHint(1)
			.setPageSize(100)
			.build();

		Query query = wellColl.orderBy("createdAt", Query.Direction.DESCENDING);

		FirestorePagingOptions<WellItem> options = new FirestorePagingOptions.Builder<WellItem>()
			.setLifecycleOwner(this)
			.setQuery(query, config, WellItem.class).build();

		adapter = new WellAdapter(options, mContext, "vertical");
		binding.rvWell.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager
			.HORIZONTAL, false));
		binding.rvWell.setAdapter(adapter);

		adapter.setOnStateChangeListener(e -> {
			if (e != null) showError(e);
			if (binding.progressbar.getVisibility() == View.VISIBLE) dismissProgress();

			if (adapter.getItemCount() > 0) {
				binding.tvNotFoundWell.setVisibility(View.GONE);
				return;
			}
			binding.tvNotFoundWell.setVisibility(View.VISIBLE);
		});
	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	private void showProgress() {
		binding.progressbar.setVisibility(View.VISIBLE);
		binding.layoutMain.setVisibility(View.INVISIBLE);
	}

	private void dismissProgress() {
		binding.progressbar.setVisibility(View.GONE);
		binding.layoutMain.setAnimation(animation);
		binding.layoutMain.setVisibility(View.VISIBLE);
	}

	public interface ListenerChange {
		void onListenerChange(int idMenu);
	}
}