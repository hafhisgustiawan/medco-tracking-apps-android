package com.medco.trackingapp.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
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
import com.medco.trackingapp.databinding.ActivityWellBinding;
import com.medco.trackingapp.fragment.CautionFragment;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.ReportItem;
import com.medco.trackingapp.model.WellItem;

public class WellActivity extends BaseActivity {

	private static final String TAG = "WellActivity";
	private Context mContext;
	private ActivityWellBinding binding;
	private Animation animation;
	private SnackbarHelper snackbarHelper;
	private FragmentManager fragmentManager;
	private CollectionReference reportColl;
	private DocumentReference currentUserRef;
	private DocumentReference currentWellRef;
	private WellItem mWellItem;
	private ReportAdapter reportAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		fragmentManager = getSupportFragmentManager();
		binding = ActivityWellBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
		snackbarHelper = new SnackbarHelper(findViewById(android.R.id.content), null);

		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser == null) return;
		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		reportColl = firebaseFirestore.collection(getString(R.string.collection_report));
		currentUserRef = firebaseFirestore.collection(getString(R.string.collection_user))
			.document(firebaseUser.getUid());

		Intent intent = getIntent();
		String path = intent.getStringExtra("path");
		if (path == null) return;
		currentWellRef = firebaseFirestore.document(path);
		binding.setCurrentWellRef(currentWellRef);

		initViews();
		initListeners();
	}

	@Override
	public void initViews() {
		binding.setUserRef(currentUserRef);

		showProgress();
		currentWellRef.get().addOnCompleteListener(tsk -> {
			dismissProgress();
			if (!tsk.isSuccessful()) {
				showError(tsk.getException());
				return;
			}

			if (!tsk.getResult().exists()) {
				showDeletedView();
				return;
			}

			mWellItem = tsk.getResult().toObject(WellItem.class);
			binding.setWellItem(mWellItem);

			initRecyclerReport();
		});
	}

	@SuppressLint("NonConstantResourceId")
	@Override
	public void initListeners() {
		binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

		binding.tvCoordinates.setOnClickListener(view -> {
			if (mWellItem == null) return;
			if (mWellItem.getLocation() == null) return;
			Uri uri = Uri.parse("google.navigation:q=" + mWellItem.getLocation()
				.getLatitude() + "," + mWellItem.getLocation().getLongitude());
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.setPackage("com.google.android.apps.maps");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				snackbarHelper.show("Tidak dapat membuka google maps", Snackbar
					.LENGTH_INDEFINITE);
			}
		});

		binding.btnMoreOption.setOnClickListener(view -> {
			if (currentWellRef == null) return;
			PopupMenu popupMenu = new PopupMenu(mContext, binding.btnMoreOption);
			popupMenu.inflate(R.menu.delete_edit_menu);
			popupMenu.setOnMenuItemClickListener(item -> {
				switch (item.getItemId()) {
					case R.id.action_edit:
						Intent intent = new Intent(mContext, ManageWellActivity.class);
						intent.putExtra("path", currentWellRef.getPath());
						startActivity(intent);
						break;
					case R.id.action_delete:
						new AlertDialog.Builder(mContext)
							.setMessage("Anda yakin ingin menghapus data sumur ini?")
							.setNegativeButton("Tidak", null)
							.setPositiveButton("Ya", (dialogInterface, i) ->
								deleteWellData()).create().show();
						break;
				}
				return false;
			});
			popupMenu.show();
		});

		binding.btnRoute.setOnClickListener(view -> {
			if (currentWellRef == null) return;
			Intent intent = new Intent(mContext, DirectionActivity.class);
			intent.putExtra("path", currentWellRef.getPath());
			mContext.startActivity(intent);
		});
	}

	private void initRecyclerReport() {
		if (currentWellRef == null) return;
		PagedList.Config config = new PagedList.Config.Builder()
			.setInitialLoadSizeHint(1)
			.setPageSize(100)
			.build();

		Query query = reportColl.orderBy("createdAt", Query.Direction.DESCENDING);

		FirestorePagingOptions<ReportItem> options = new FirestorePagingOptions.Builder
			<ReportItem>().setLifecycleOwner(this).setQuery(query, config, ReportItem.class)
			.build();

		reportAdapter = new ReportAdapter(options, mContext);
		binding.rvReport.setLayoutManager(new LinearLayoutManager(mContext));
		binding.rvReport.setAdapter(reportAdapter);

		reportAdapter.setOnStateChangeListener(e -> {
			if (e != null) showError(e);
			if (binding.progressbar.getVisibility() == View.VISIBLE) dismissProgress();

			if (reportAdapter.getItemCount() > 0) {
				binding.tvNotFound.setVisibility(View.GONE);
				return;
			}
			binding.tvNotFound.setVisibility(View.VISIBLE);
		});
	}

	private void deleteWellData() {
		if (currentWellRef == null) return;
		showProgress();
		reportColl.whereEqualTo("wellRef", currentWellRef).get()
			.addOnCompleteListener(task -> {
				dismissProgress();
				if (!task.isSuccessful()) {
					showError(task.getException());
					return;
				}
				if (!task.getResult().isEmpty()) {
					snackbarHelper.show("Data sumur ini tidak boleh dihapus ketika " +
							"sudah tertaut dengan laporan pemantauan",
						Snackbar.LENGTH_INDEFINITE);
					return;
				}
				showProgress();
				currentWellRef.delete().addOnCompleteListener(tsk -> {
					dismissProgress();
					if (!tsk.isSuccessful()) {
						showError(tsk.getException());
						return;
					}
					initViews();
				});
			});
	}

	private void showDeletedView() {
		if (isFinishing() || fragmentManager.isDestroyed()) {
			Toast.makeText(mContext, "Data sumur sudah dihapus!", Toast.LENGTH_SHORT)
				.show();
			getOnBackPressedDispatcher().onBackPressed();
			return;
		}

		CautionFragment fragment = new CautionFragment(R.raw.delete,
			"Data sumur sudah dihapus!");
		fragment.setCancelable(false);
		fragment.ListenerApiClose(() -> getOnBackPressedDispatcher().onBackPressed());
		fragment.show(fragmentManager, TAG);
	}


	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	private void showProgress() {
		binding.progressbar.setVisibility(View.VISIBLE);
		binding.layoutMain.setVisibility(View.GONE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("SetTextI18n")
	private void dismissProgress() {
		binding.progressbar.setVisibility(View.GONE);
		binding.layoutMain.setAnimation(animation);
		binding.layoutMain.setVisibility(View.VISIBLE);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}
}