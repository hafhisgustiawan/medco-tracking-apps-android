package com.medco.trackingapp.adapter.binding;

import android.net.Uri;

import androidx.databinding.BindingAdapter;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.medco.trackingapp.R;
import com.medco.trackingapp.model.WellItem;

import java.util.ArrayList;
import java.util.List;

public class WellActivityBindAdapter {
	@BindingAdapter("loadImgSliderWell")
	public static void loadImgSliderWell(ImageSlider imgSlider, WellItem item) {
		if (item == null || item.getImages() == null) return;
		if (item.getImages().size() == 0) return;

		FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
		List<Task<Uri>> tasks = new ArrayList<>();

		item.getImages().forEach(s -> {
			StorageReference fileRef = firebaseStorage.getReference(imgSlider.getContext()
				.getString(R.string.collection_well)).child(s);
			tasks.add(fileRef.getDownloadUrl());
		});

		Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
			if (!task.isSuccessful()) return;
			List<SlideModel> listSlider = new ArrayList<>();
			tasks.forEach(uriTask -> listSlider.add(new SlideModel(uriTask
				.getResult().toString(), "", ScaleTypes
				.CENTER_CROP)));
			imgSlider.setImageList(listSlider);
		});
	}
}
