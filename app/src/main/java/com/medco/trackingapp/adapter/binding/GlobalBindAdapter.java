package com.medco.trackingapp.adapter.binding;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.medco.trackingapp.R;
import com.medco.trackingapp.helper.GlideHelper;
import com.medco.trackingapp.model.ImageItem;
import com.medco.trackingapp.model.UserItem;
import com.medco.trackingapp.model.WellItem;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GlobalBindAdapter {
	private static Drawable getProgressBarDrawable(final Context context) {
		TypedValue value = new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.progressBarStyleSmall, value,
			false);
		int progressBarStyle = value.data;
		int[] attributes = new int[]{android.R.attr.indeterminateDrawable};
		TypedArray typedArray = context.obtainStyledAttributes(progressBarStyle, attributes);
		Drawable drawable = typedArray.getDrawable(0);
		typedArray.recycle();

		return drawable;
	}

	@BindingAdapter(value = {"android:progressVisible"})
	public static void setButtonLoading(MaterialButton button, boolean loading) {
		if (loading) {
			Drawable drawable = button.getIcon();
			if (!(drawable instanceof Animatable)) {
				drawable = getProgressBarDrawable(button.getContext());

				button.setIcon(drawable);
			}
			((Animatable) drawable).start();
		} else {
			button.setIcon(null);
		}
	}

	@BindingAdapter("imgBtnVisibleForAdmin")
	public static void imgBtnVisibleForAdmin(ImageButton imgBtn, DocumentReference userRef) {
		imgBtn.setVisibility(View.GONE);
		if (userRef == null) return;
		userRef.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			UserItem userItem = task.getResult().toObject(UserItem.class);
			if (userItem == null) return;
			if (!Objects.equals(userItem.getRole(), "admin")) return;
			imgBtn.setVisibility(View.VISIBLE);
		});
	}

	@BindingAdapter("constraintLayoutVisibleForAdmin")
	public static void constraintLayoutVisibleForAdmin(ConstraintLayout view,
																										 DocumentReference userRef) {
		view.setVisibility(View.GONE);
		if (userRef == null) return;
		userRef.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			UserItem userItem = task.getResult().toObject(UserItem.class);
			if (userItem == null) return;
			if (!Objects.equals(userItem.getRole(), "admin")) return;
			view.setVisibility(View.VISIBLE);
		});
	}

	@BindingAdapter("imgBtnVisibleForUser")
	public static void imgBtnVisibleForUser(ImageButton imgBtn, DocumentReference userRef) {
		imgBtn.setVisibility(View.GONE);
		if (userRef == null) return;
		userRef.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			UserItem userItem = task.getResult().toObject(UserItem.class);
			if (userItem == null) return;
			if (!Objects.equals(userItem.getRole(), "user")) return;
			imgBtn.setVisibility(View.VISIBLE);
		});
	}

	@BindingAdapter("constraintLayoutVisibleForUser")
	public static void constraintLayoutVisibleForUser(ConstraintLayout view,
																										DocumentReference userRef) {
		view.setVisibility(View.GONE);
		if (userRef == null) return;
		userRef.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			UserItem userItem = task.getResult().toObject(UserItem.class);
			if (userItem == null) return;
			if (!Objects.equals(userItem.getRole(), "user")) return;
			view.setVisibility(View.VISIBLE);
		});
	}

	@BindingAdapter("formatTimestampTv")
	public static void formatTimestampTv(TextView tv, Timestamp timestamp) {
		if (timestamp == null) return;
		Locale localeID = new Locale("in", "ID");
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("E, dd MMM yyy, H:mm",
			localeID);
		tv.setText(dateTimeFormat.format(timestamp.toDate()));
	}

	@BindingAdapter("setAddressByGeoPointIntoTv")
	public static void setAddressByGeoPointIntoTv(TextView tv, GeoPoint location) {
		if (location == null) return;

		Geocoder geocoder = new Geocoder(tv.getContext());
		try {
			List<Address> addressList = geocoder.getFromLocation(location.getLatitude(),
				location.getLongitude(), 1);
			if (addressList == null) return;
			tv.setText(addressList.get(0).getAddressLine(0));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@BindingAdapter("setAddressByGeoPointIntoEt")
	public static void setAddressByGeoPointIntoEt(TextInputEditText et, GeoPoint location) {
		if (location == null) return;

		Geocoder geocoder = new Geocoder(et.getContext());
		try {
			List<Address> addressList = geocoder.getFromLocation(location.getLatitude(),
				location.getLongitude(), 1);
			if (addressList == null) return;
			et.setText(addressList.get(0).getAddressLine(0));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@BindingAdapter("listImageSource")
	public static void loadImageList(ShapeableImageView imageView, ImageItem item) {
		Context ctx = imageView.getContext();
		if (!(new GlideHelper().isValidCtx(ctx))) return;
		if (item == null) {
			Glide.with(ctx)
				.load(R.drawable.baseline_add_circle_gray_48)
				.centerInside()
				.into(imageView);
			return;
		}

		if (item.getUri() != null) {
			Log.d("TAG", "loadImageList: DARI URI");
			Glide.with(imageView.getContext()).load(item.getUri()).
				fitCenter().centerCrop().into(imageView);

			return;
		}

		if (item.getImage() == null) return;
		Log.d("TAG", "loadImageList: DARI IMAGE");
		FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
		StorageReference fileRef = firebaseStorage.getReference(item.getType()).child(item.getImage());
		fileRef.getDownloadUrl().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			Glide.with(ctx).load(task.getResult()).fitCenter().centerCrop()
				.into(imageView);
		});
	}

	@SuppressLint("SetTextI18n")
	@BindingAdapter("setTotalReportTv")
	public static void setTotalReportTv(TextView tv, DocumentReference wellRef) {
		if (wellRef == null) return;
		FirebaseFirestore firestore = FirebaseFirestore.getInstance();
		CollectionReference reportColl = firestore.collection(tv.getContext().getString(R
			.string.collection_report));

		reportColl.whereEqualTo("wellRef", wellRef).get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			tv.setText("Total laporan (" + task.getResult().size() + ")");
		});
	}

	@BindingAdapter("setBottomNavMenu")
	public static void setBottomNavMenu(ChipNavigationBar navMenu, DocumentReference userRef) {
		if (userRef == null) return;
		userRef.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			UserItem userItem = task.getResult().toObject(UserItem.class);
			if (userItem == null) return;
			if (Objects.equals(userItem.getRole(), "admin")) {
				navMenu.setMenuResource(R.menu.bottom_navigation);
			} else {
				navMenu.setMenuResource(R.menu.bottom_navigation_user);
			}
			navMenu.setItemSelected(R.id.nav_home, true);
		});
	}

	@SuppressLint("SetTextI18n")
	@BindingAdapter("initCategoriesWellIntoTv")
	public static void initCategoriesWellIntoTv(TextView et, String cat) {
		if (cat == null) return;

		if (cat.equals("as")) {
			et.setText("AS (Alur Siwah)");
			return;
		}

		if (cat.equals("jr")) {
			et.setText("JR (Julok Rayeuk)");
			return;
		}

		et.setText("Other Well (Sumur Tua)");
	}

	@BindingAdapter("btnEnableIfThereLocation")
	public static void btnEnableIfThereLocation(MaterialButton btn, WellItem item) {
		if (item == null || item.getLocation() == null) {
			btn.setEnabled(false);
			return;
		}
		btn.setEnabled(true);
	}

}
