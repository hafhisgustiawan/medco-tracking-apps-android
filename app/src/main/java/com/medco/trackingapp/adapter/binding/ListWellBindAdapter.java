package com.medco.trackingapp.adapter.binding;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.medco.trackingapp.R;
import com.medco.trackingapp.helper.GlideHelper;
import com.medco.trackingapp.model.WellItem;

public class ListWellBindAdapter {
	@BindingAdapter("wellImageSource")
	public static void wellImageSource(ShapeableImageView img, WellItem item) {
		if (item == null) return;
		if (!(new GlideHelper().isValidCtx(img.getContext()))) return;
		if (item.getImages() == null) return;
		if (item.getImages().size() == 0) return;
		FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
		StorageReference fileRef = firebaseStorage.getReference(img.getContext()
			.getString(R.string.collection_well)).child(item.getImages().get(0));
		fileRef.getDownloadUrl().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			Glide.with(img.getContext())
				.load(task.getResult())
				.fitCenter().centerCrop()
				.into(img);
		});
	}
}
