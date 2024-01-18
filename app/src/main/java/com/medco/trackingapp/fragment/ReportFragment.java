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
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
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
import com.medco.trackingapp.model.FilterItem;
import com.medco.trackingapp.model.ReportItem;
import com.medco.trackingapp.model.UserItem;
import com.medco.trackingapp.model.viewmodel.FilterViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
	private UserItem mUserItem;
	private FilterViewModel mViewModel;
	private Locale localeID;
	private SimpleDateFormat dateFormat;

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

		mViewModel = new ViewModelProvider(this).get(FilterViewModel.class);

		localeID = new Locale("in", "ID");
		dateFormat = new SimpleDateFormat("dd/MM/yy", localeID);

		initViews();
		initListeners();
		return binding.getRoot();
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void initViews() {
//		binding.setUserRef(currentUserRef);
		showProgress();
		currentUserRef.get().addOnCompleteListener(task -> {
			dismissProgress();
			if (!task.isSuccessful()) {
				showError(task.getException());
				return;
			}
			mUserItem = task.getResult().toObject(UserItem.class);
		});

		mViewModel.getFilterState().observe(getViewLifecycleOwner(), filterItem -> {
			if (filterItem == null) return;
			if (filterItem.getPair() != null) {
				binding.tvResult.setText("Hasil " + dateFormat.format(new Date(filterItem.getPair()
					.first)) + " - " + dateFormat.format(new Date(filterItem.getPair().second)));
			} else {
				binding.tvResult.setText(getString(R.string.txt_placeholder_result));
			}

			binding.tvFilter.setText(filterItem.getPair() != null ? "Rentang tanggal" : Objects
				.equals(filterItem.getLabel(), "mine") ? "Milik saya" : "Semua");
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
			if (mUserItem == null || mUserItem.getRole() == null) return;
			PopupMenu popupMenu = new PopupMenu(mContext, binding.tvFilter);

			if (Objects.equals(mUserItem.getRole(), "admin")) {
				popupMenu.inflate(R.menu.filter_report_menu_admin);
			} else {
				popupMenu.inflate(R.menu.filter_report_menu);
			}

			popupMenu.setOnMenuItemClickListener(item -> {
				FilterItem filterItem = mViewModel.getFilterState().getValue();
				if (filterItem == null) return false;
				switch (item.getItemId()) {
					case R.id.action_all:
						filterItem.setPair(null);
						filterItem.setLabel("");
						mViewModel.setFilterState(filterItem);
						break;
					case R.id.action_mine:
						filterItem.setPair(null);
						filterItem.setLabel("mine");
						mViewModel.setFilterState(filterItem);
						break;
					case R.id.action_select_range:
						showRangeDatePicker();
						break;
				}
				return false;
			});
			popupMenu.show();
		});
	}

	private void showRangeDatePicker() {
		MaterialDatePicker<Pair<Long, Long>> materialDatePicker = MaterialDatePicker.Builder
			.dateRangePicker()
			.setTitleText("Pilih Rentang Tanggal")
			.setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(),
				MaterialDatePicker.todayInUtcMilliseconds())).build();
		materialDatePicker.addOnPositiveButtonClickListener(selection -> {
			FilterItem item = mViewModel.getFilterState().getValue();
			if (item == null) return;
			item.setPair(selection);
			item.setLabel("");
			mViewModel.setFilterState(item);
		});
		materialDatePicker.show(getChildFragmentManager(), TAG);
	}

	private void initRecyclerView() {
		if (getQuery() == null) return;
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

			if (adapter.getItemCount() > 0) {
				binding.tvNotFound.setVisibility(View.GONE);
				return;
			}
			binding.tvNotFound.setVisibility(View.VISIBLE);
		});
	}

	private Query getQuery() {
		FilterItem item = mViewModel.getFilterState().getValue();
		if (item == null || currentUserRef == null) return null;

		if (item.getPair() != null) {
			return reportColl
				.whereGreaterThanOrEqualTo("createdAt", getTimestampDayStart(item.getPair().first))
				.whereLessThanOrEqualTo("createdAt", getTimestampDayEnd(item.getPair().second))
				.orderBy("createdAt", Query.Direction.DESCENDING);
		}


		if (Objects.equals(item.getLabel(), "mine")) {
			return reportColl.whereEqualTo("userRef", currentUserRef).orderBy("createdAt",
				Query.Direction.DESCENDING);
		}
		return reportColl.orderBy("createdAt", Query.Direction.DESCENDING);
	}

	private Timestamp getTimestampDayStart(Long timeFirst) {
		Date date = new Date(timeFirst);
		Calendar calendarStart = Calendar.getInstance(localeID);
		calendarStart.setTime(date);
		calendarStart.set(Calendar.HOUR_OF_DAY, 0);
		calendarStart.set(Calendar.MINUTE, 0);
		calendarStart.set(Calendar.SECOND, 0);

		return new Timestamp(calendarStart.getTime());
	}

	private Timestamp getTimestampDayEnd(Long timeSecond) {
		Date date = new Date(timeSecond);
		Calendar calendarStart = Calendar.getInstance(localeID);
		calendarStart.setTime(date);
		calendarStart.set(Calendar.HOUR_OF_DAY, 23);
		calendarStart.set(Calendar.MINUTE, 59);
		calendarStart.set(Calendar.SECOND, 59);

		return new Timestamp(calendarStart.getTime());
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