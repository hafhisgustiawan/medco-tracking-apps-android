package com.medco.trackingapp.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.ActivityMainBinding;
import com.medco.trackingapp.fragment.AccountFragment;
import com.medco.trackingapp.fragment.HomeFragment;
import com.medco.trackingapp.fragment.ReportFragment;
import com.medco.trackingapp.fragment.UserFragment;
import com.medco.trackingapp.fragment.WellFragment;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.UserItem;

public class MainActivity extends BaseActivity implements FirebaseAuth.AuthStateListener {

	public static final String TAG = "MainActivity";
	//alert
	private static final int TIME_INTERVAL = 2000; // Waktu dalam milidetik
	private Context mContext;
	private FragmentManager fragmentManager;
	private ActivityMainBinding binding;
	private SnackbarHelper snackbarHelper;
	//firebase
	private FirebaseAuth firebaseAuth;
	private FirebaseFirestore firebaseFirestore;
	private FirebaseMessaging firebaseMessaging;
	private DocumentReference currentUserRef;
	//global value
	private Animation animation;
	private long mBackPressed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		fragmentManager = getSupportFragmentManager();
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		snackbarHelper = new SnackbarHelper(findViewById(android.R.id.content), null);
		animation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);

		firebaseMessaging = FirebaseMessaging.getInstance();
		firebaseAuth = FirebaseAuth.getInstance();
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		firebaseFirestore = FirebaseFirestore.getInstance();

		if (firebaseUser == null) {
			firebaseAuth.signOut();
			return;
		}

		if (firebaseUser.isAnonymous()) {
			firebaseAuth.signOut();
			return;
		}

		currentUserRef = firebaseFirestore.collection(getString(R.string.collection_user))
			.document(firebaseUser.getUid());

		initViews();
		initListeners();
	}

	@Override
	public void initViews() {
		showProgress();
		currentUserRef.get().addOnCompleteListener(task -> {
			dismissProgress();
			if (!task.isSuccessful()) {
				showError(task.getException());
				return;
			}

			if (!task.getResult().exists()) {
				firebaseAuth.signOut();
				return;
			}

			UserItem userItem = task.getResult().toObject(UserItem.class);
			if (userItem == null) return;

			if (userItem.getRole() == null) {
				// BLOCKED BY ADMIN KETIKA ROLE NULL
				firebaseAuth.signOut();
				return;
			}

			binding.setUserRef(currentUserRef);
			Fragment selectedFragment = initHomeFragment();
			if (selectedFragment == null) return;
			fragmentManager.beginTransaction().replace(R.id.fragment_container,
				selectedFragment).commit();
			binding.bottomNavigation.setItemSelected(R.id.nav_home, true);

		});
	}

	@SuppressLint("NonConstantResourceId")
	@Override
	public void initListeners() {
		binding.bottomNavigation.setOnItemSelectedListener(i -> {
			Fragment selectedFragment = initHomeFragment();

			switch (i) {
				case R.id.nav_home:
					selectedFragment = initHomeFragment();
					break;
				case R.id.nav_well:
					selectedFragment = new WellFragment();
					break;
				case R.id.nav_report:
					selectedFragment = new ReportFragment();
					break;
				case R.id.nav_user:
					selectedFragment = new UserFragment();
					break;
				case R.id.nav_acc:
					selectedFragment = new AccountFragment();
					break;
			}
			if (selectedFragment == null) return;
			getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim
				.slide_in_left, android.R.anim.slide_out_right).replace(R.id.fragment_container,
				selectedFragment).commit();
		});
	}

	private Fragment initHomeFragment() {
		if (fragmentManager.isDestroyed()) return null;
		HomeFragment fragment = new HomeFragment();
		fragment.ListenerApiChange(idMenu -> binding.bottomNavigation
			.setItemSelected(idMenu, true));
		return fragment;
	}

	@Override
	public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
		if (firebaseAuth.getCurrentUser() == null) {
			Intent intent = new Intent(mContext, AuthActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
			return;
		}

		// UPDATE FCM TOKEN
		showProgress();
		currentUserRef = firebaseFirestore.collection(getString(R.string.collection_user))
			.document(firebaseAuth.getCurrentUser().getUid());
		firebaseMessaging.getToken()
			.addOnCompleteListener(task -> {
				if (!task.isSuccessful()) {
					dismissProgress();
					showError(task.getException());
					return;
				}
				currentUserRef
					.update("uidDevice", task.getResult())
					.addOnCompleteListener(tsk -> {
						dismissProgress();
						if (!tsk.isSuccessful()) {
							showError(tsk.getException());
							return;
						}
						Log.d(TAG, "updateUid: Berhasil updateUID");
					});
			});
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

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}

	@Override
	protected void onStart() {
		super.onStart();
		FirebaseAuth.getInstance().addAuthStateListener(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FirebaseAuth.getInstance().removeAuthStateListener(this);
	}

	@Override
	public void onBackPressed() {
		if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
			super.onBackPressed();
			return;
		} else {
			Toast.makeText(mContext, "Tekan lagi untuk keluar",
				Toast.LENGTH_SHORT).show();
		}
		mBackPressed = System.currentTimeMillis();
	}
}