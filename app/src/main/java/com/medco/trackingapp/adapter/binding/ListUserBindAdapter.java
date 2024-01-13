package com.medco.trackingapp.adapter.binding;

import android.content.Context;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.medco.trackingapp.R;
import com.medco.trackingapp.helper.GlideHelper;

public class ListUserBindAdapter {
	@BindingAdapter("loadUserImg")
	public static void loadUserImg(ShapeableImageView imgView, String photo) {
		if (photo == null) {
			Glide.with(imgView.getContext()).load(R.drawable.person_icon).fitCenter()
				.centerCrop().into(imgView);
			return;
		}
		Context ctx = imgView.getContext();
		if (!(new GlideHelper().isValidCtx(ctx))) return;

		FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
		StorageReference ref = firebaseStorage.getReference(ctx.getString(R.string
			.collection_user)).child(photo);

		ref.getDownloadUrl().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			Glide.with(imgView.getContext()).load(task.getResult()).fitCenter()
				.centerCrop().into(imgView);
		});
	}
}
