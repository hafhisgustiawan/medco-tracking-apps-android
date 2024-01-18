package com.medco.trackingapp.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.ActivityReportBinding;
import com.medco.trackingapp.fragment.CautionFragment;
import com.medco.trackingapp.fragment.ManageReportFragment;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.ReportItem;

public class ReportActivity extends BaseActivity {

	public static final String TAG = "ReportActivity";
	private Context mContext;
	private ActivityReportBinding binding;
	private Animation animation;
	private SnackbarHelper snackbarHelper;
	private FragmentManager fragmentManager;
	private DocumentReference currentReportRef;
	private DocumentReference currentUserRef;
	private DocumentSnapshot mReportSnap;
	private ReportItem mReportItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		fragmentManager = getSupportFragmentManager();
		mContext = this;
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
		snackbarHelper = new SnackbarHelper(findViewById(android.R.id.content), null);
		binding = ActivityReportBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser == null) return;
		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		currentUserRef = firebaseFirestore.collection(getString(R.string.collection_user))
			.document(firebaseUser.getUid());

		Intent intent = getIntent();
		String path = intent.getStringExtra("path");
		if (path == null) return;
		currentReportRef = firebaseFirestore.document(path);

		initViews();
		initListeners();
	}

	@Override
	public void initViews() {
		showProgress();
		currentReportRef.get().addOnCompleteListener(tsk -> {
			dismissProgress();
			if (!tsk.isSuccessful()) {
				showError(tsk.getException());
				return;
			}

			if (!tsk.getResult().exists()) {
				showDeletedView();
				return;
			}

			mReportSnap = tsk.getResult();

			mReportItem = tsk.getResult().toObject(ReportItem.class);
			binding.setReportItem(mReportItem);
		});
	}

	@SuppressLint("NonConstantResourceId")
	@Override
	public void initListeners() {
		binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

		binding.tvWellName.setOnClickListener(view -> {
			if (mReportItem == null || mReportItem.getWellRef() == null) return;
			Intent intent = new Intent(mContext, WellActivity.class);
			intent.putExtra("path", mReportItem.getWellRef().getPath());
			startActivity(intent);
		});

		binding.btnMoreOption.setOnClickListener(view -> {
			if (currentReportRef == null) return;
			PopupMenu popupMenu = new PopupMenu(mContext, binding.btnMoreOption);
			popupMenu.inflate(R.menu.delete_edit_menu);
			popupMenu.setOnMenuItemClickListener(item -> {
				switch (item.getItemId()) {
					case R.id.action_edit:
						if (fragmentManager.isDestroyed() || mReportSnap == null) return false;

						if (mReportItem == null || mReportItem.getWellRef() == null) return false;

						ManageReportFragment fragment = new ManageReportFragment(mReportItem.getWellRef(),
							mReportSnap);
						fragment.setCancelable(false);
						fragment.ListenerApiClose(selector -> {
							fragment.dismiss();
							if (selector == 1) {
								Toast.makeText(mContext, "Berhasil memperbarui laporan", Toast
									.LENGTH_SHORT).show();
								initViews();
							}
						});
						fragment.show(fragmentManager, TAG);
						break;
					case R.id.action_delete:
						new AlertDialog.Builder(mContext)
							.setMessage("Anda yakin ingin menghapus data laporan ini?")
							.setNegativeButton("Tidak", null)
							.setPositiveButton("Ya", (dialogInterface, i) ->
								deleteReportData()).create().show();
						break;
				}
				return false;
			});
			popupMenu.show();
		});
	}

	private void deleteReportData() {
		showProgress();
		currentReportRef.delete().addOnCompleteListener(tsk -> {
			dismissProgress();
			if (!tsk.isSuccessful()) {
				showError(tsk.getException());
				return;
			}
			initViews();
		});
	}

	private void showDeletedView() {
		if (isFinishing() || fragmentManager.isDestroyed()) {
			Toast.makeText(mContext, "Data laporan sudah dihapus!", Toast.LENGTH_SHORT)
				.show();
			getOnBackPressedDispatcher().onBackPressed();
			return;
		}

		CautionFragment fragment = new CautionFragment(R.raw.delete,
			"Data laporan sudah dihapus!");
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