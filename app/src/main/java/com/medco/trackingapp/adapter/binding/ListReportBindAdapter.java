package com.medco.trackingapp.adapter.binding;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.medco.trackingapp.R;
import com.medco.trackingapp.helper.GlideHelper;
import com.medco.trackingapp.model.ReportItem;
import com.medco.trackingapp.model.UserItem;

public class ListReportBindAdapter {
	@BindingAdapter("reportImageSource")
	public static void reportImageSource(ShapeableImageView img, ReportItem item) {
		if (item == null) return;
		if (!(new GlideHelper().isValidCtx(img.getContext()))) return;
		if (item.getImages() == null) return;
		if (item.getImages().size() == 0) return;
		FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
		StorageReference fileRef = firebaseStorage.getReference(img.getContext()
			.getString(R.string.collection_report)).child(item.getImages().get(0));
		fileRef.getDownloadUrl().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			Glide.with(img.getContext())
				.load(task.getResult())
				.fitCenter().centerCrop()
				.into(img);
		});
	}

	@BindingAdapter("initWellNameFromRef")
	public static void initWellNameFromRef(TextView tv, DocumentReference wellRef) {
		if (wellRef == null) return;

		wellRef.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			String name = task.getResult().getString("name");
			if (name == null) return;
			tv.setText(name);
		});
	}

	@SuppressLint("SetTextI18n")
	@BindingAdapter("initWellCategoriesFromRef")
	public static void initWellCategoriesFromRef(TextView tv, DocumentReference wellRef) {
		if (wellRef == null) return;

		wellRef.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			String cat = task.getResult().getString("category");
			if (cat == null) return;
			if (cat.equals("as")) {
				tv.setText("AS (Alur Siwah)");
				return;
			}

			if (cat.equals("jr")) {
				tv.setText("JR (Julok Rayeuk)");
				return;
			}

			tv.setText("Other Well");
		});
	}

	@BindingAdapter("initUserNameFromRef")
	public static void initUserNameFromRef(TextView tv, DocumentReference userRef) {
		if (userRef == null) return;

		userRef.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			UserItem item = task.getResult().toObject(UserItem.class);
			if (item == null) return;
			tv.setText(item.getName());
		});
	}

	@BindingAdapter("initUserPhotoFromRef")
	public static void initUserPhotoFromRef(ShapeableImageView img, DocumentReference userRef) {
		Context ctx = img.getContext();
		if (userRef == null) return;
		if (!(new GlideHelper().isValidCtx(ctx))) return;

		userRef.get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			UserItem item = task.getResult().toObject(UserItem.class);
			if (item == null) return;

			if (item.getPhoto() == null) {
				Glide.with(ctx).load(R.drawable.person_icon).fitCenter()
					.centerCrop().into(img);
				return;
			}

			FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
			StorageReference ref = firebaseStorage.getReference(ctx.getString(R.string
				.collection_user)).child(item.getPhoto());

			ref.getDownloadUrl().addOnCompleteListener(t -> {
				if (!t.isSuccessful()) return;
				Glide.with(img.getContext()).load(t.getResult()).fitCenter()
					.centerCrop().into(img);
			});
		});
	}
}
