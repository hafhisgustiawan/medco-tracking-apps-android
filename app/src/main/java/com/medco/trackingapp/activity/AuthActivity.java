package com.medco.trackingapp.activity;

import static com.medco.trackingapp.BuildConfig.WEB_CLIENT_ID_AUTH;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.medco.trackingapp.R;
import com.medco.trackingapp.databinding.ActivityAuthBinding;
import com.medco.trackingapp.fragment.LoginFragment;
import com.medco.trackingapp.helper.SnackbarHelper;
import com.medco.trackingapp.model.UserItem;

import java.util.ArrayList;
import java.util.List;

public class AuthActivity extends BaseActivity {

	public static final String TAG = AuthActivity.class.getSimpleName();
	private Context mContext;
	private ActivityAuthBinding binding;
	private SnackbarHelper snackbarHelper;
	private FragmentManager fragmentManager;

	//firebase
	private FirebaseAuth firebaseAuth;
	private FirebaseUser firebaseUser;
	private CollectionReference userColl;
	private DocumentReference currentUserRef;
	private GoogleSignInClient mGoogleSignInClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		mContext = this;
		fragmentManager = getSupportFragmentManager();
		binding = ActivityAuthBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		snackbarHelper = new SnackbarHelper(findViewById(android.R.id.content), null);

		//firebase
		firebaseAuth = FirebaseAuth.getInstance();
		firebaseUser = firebaseAuth.getCurrentUser();
		FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
		userColl = firebaseFirestore.collection(getString(R.string.collection_user));

		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions
			.DEFAULT_SIGN_IN)
			.requestIdToken(WEB_CLIENT_ID_AUTH)
			.requestEmail().build();
		mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);

		if (firebaseUser != null) {
			currentUserRef = firebaseFirestore.collection(getString(R.string.collection_user))
				.document(firebaseUser.getUid());
		}

		initViews();
		initListeners();
	}

	@Override
	public void initViews() {
		if (firebaseUser == null) return;
		currentUserRef.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) {
				showError(task.getException());
				return;
			}

			if (task.getResult().exists()) {
				goToDestination(firebaseUser);
				return;
			}

			Log.d(TAG, "initViews: Data pengguna tidak ditemukan, tapi firebase user exist");
			firebaseAuth.signOut();
		});
	}

	ActivityResultLauncher<Intent> signInGoogleLauncher = registerForActivityResult(
		new ActivityResultContracts.StartActivityForResult(),
		result -> {
			if (result.getResultCode() == Activity.RESULT_OK) {
				Task<GoogleSignInAccount> task = GoogleSignIn
					.getSignedInAccountFromIntent(result.getData());
				try {
					// Google Sign In was successful, authenticate with Firebase
					GoogleSignInAccount account = task.getResult(ApiException.class);
					Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
					checkExistanceUserByEmail(account.getEmail(), account.getIdToken());
				} catch (ApiException e) {
					Log.e(TAG, "Google sign in failed", e);
					snackbarHelper.show("Gagal mendapatkan info Email pada perangkat",
						Snackbar.LENGTH_LONG);
				}
			}
		}
	);

	@Override
	public void initListeners() {
		binding.tvForgetPassword.setOnClickListener(view -> startActivity(new Intent(
			this, ResetPasswordActivity.class)));

		binding.btnLoginWithGoogle.setOnClickListener(view -> {
			Intent signInIntent = mGoogleSignInClient.getSignInIntent();
			signInGoogleLauncher.launch(signInIntent);
		});

		binding.btnLoginEmail.setOnClickListener(view -> {
			LoginFragment fragment = new LoginFragment();
			fragment.setCancelable(false);
			fragment.ListenerApiClose((user) -> {
				if (user == null) {
					fragment.dismiss();
					return;
				}
				goToDestination(user);
			});
			if (!fragmentManager.isDestroyed()) fragment.show(fragmentManager, TAG);
		});
	}

	private void checkExistanceUserByEmail(String email, String idToken) {
		showProgress();
		userColl.whereEqualTo("email", email).get().addOnCompleteListener(task -> {
			dismissProgress();
			if (!task.isSuccessful()) {
				firebaseAuth.signOut();
				showError(task.getException());
				return;
			}
			if (task.getResult().isEmpty()) {
				firebaseAuth.signOut();
				snackbarHelper.show("Email Anda tidak terdaftar dalam sistem aplikasi",
					Snackbar.LENGTH_LONG);
				return;
			}

			firebaseAuthWithGoogle(idToken);
		});
	}

	private void firebaseAuthWithGoogle(String idToken) {
		showProgress();
		AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
		firebaseAuth.signInWithCredential(credential)
			.addOnCompleteListener(this, task -> {
				dismissProgress();
				if (!task.isSuccessful()) {
					showError(task.getException());
					return;
				}

				Log.d(TAG, "signInWithCredential:success");
				firebaseUser = task.getResult().getUser();
				if (firebaseUser == null) return;
				validateLoginFirestore();
			});
	}

	private void validateLoginFirestore() {
		userColl
			.document(firebaseUser.getUid())
			.get()
			.addOnCompleteListener(task -> {
				dismissProgress();
				if (!task.isSuccessful()) {
					firebaseAuth.signOut();
					showError(task.getException());
					return;
				}

				if (!task.getResult().exists()) {
					updateDataFirestore();
					return;
				}

				UserItem userItem = task.getResult().toObject(UserItem.class);
				if (userItem == null) return;
				goToDestination(firebaseUser);
			});
	}


	private void updateDataFirestore() {
		//KALO SAMPE SINI, DATA USER INI UDAH PASTI ADA, TAPI BEDA ID AJA
		showProgress();
		userColl.whereEqualTo("email", firebaseUser.getEmail()).get()
			.addOnCompleteListener(task -> {
				dismissProgress();
				if (!task.isSuccessful()) {
					firebaseAuth.signOut();
					showError(task.getException());
					return;
				}

				if (task.getResult().isEmpty()) {
					Log.d(TAG, "updateDataFirestore: Result task kosong");
					firebaseAuth.signOut();
					return;
				}

				UserItem firstItem = task.getResult().getDocuments().get(0)
					.toObject(UserItem.class);

				if (firstItem == null) {
					Log.d(TAG, "updateDataFirestore: First item kosong");
					firebaseAuth.signOut();
					return;
				}

				// 1) TAMBAH DULU DATA BARU NYA
				showProgress();
				userColl.document(firebaseUser.getUid()).set(firstItem)
					.addOnCompleteListener(tsk -> {
						dismissProgress();
						if (!task.isSuccessful()) {
							firebaseAuth.signOut();
							showError(task.getException());
							return;
						}
						List<Task<Void>> tasks = new ArrayList<>();
						task.getResult().getDocuments().forEach(documentSnapshot -> tasks
							.add(documentSnapshot.getReference().delete()));
						// 1) HAPUS DATA LAMANYA
						deleteOldData(tasks);
					});
			});
	}

	private void deleteOldData(List<Task<Void>> tasks) {
		showProgress();
		Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> {
			dismissProgress();
			if (!t.isSuccessful()) {
				firebaseAuth.signOut();
				showError(t.getException());
				return;
			}

			goToDestination(firebaseUser);
		});
	}

	private void goToDestination(FirebaseUser user) {
		if (user == null) return;
		Intent intent = new Intent(mContext, MainActivity.class);
		startActivity(intent
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
		finish();
	}

	private void showProgress() {
		binding.progressbar.setVisibility(View.VISIBLE);
		binding.btnLoginWithGoogle.setVisibility(View.INVISIBLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	@SuppressLint("SetTextI18n")
	private void dismissProgress() {
		binding.progressbar.setVisibility(View.GONE);
		binding.btnLoginWithGoogle.setVisibility(View.VISIBLE);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	private void showError(Exception e) {
		if (e == null) return;
		snackbarHelper.show(e.getMessage(), Snackbar.LENGTH_INDEFINITE);
		Log.e(TAG, "showError: ", e);
	}
}