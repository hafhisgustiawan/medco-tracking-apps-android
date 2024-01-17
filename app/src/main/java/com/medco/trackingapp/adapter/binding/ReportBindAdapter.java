package com.medco.trackingapp.adapter.binding;

import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;

import androidx.databinding.BindingAdapter;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.medco.trackingapp.R;
import com.medco.trackingapp.model.ReportItem;

import java.util.ArrayList;
import java.util.List;

public class ReportBindAdapter {
	@BindingAdapter("imageSliderReportSource")
	public static void loadImgSliderReport(ImageSlider imgSlider, ReportItem item) {
		if (item == null || item.getImages() == null) return;
		if (item.getImages().size() == 0) return;

		FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
		List<Task<Uri>> tasks = new ArrayList<>();

		item.getImages().forEach(s -> {
			StorageReference fileRef = firebaseStorage.getReference(imgSlider.getContext()
				.getString(R.string.collection_report)).child(s);
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

	@BindingAdapter("visibleForSameUserInReport")
	public static void visibleForSameUserInReport(ImageButton imgBtn, ReportItem item) {
		imgBtn.setVisibility(View.GONE);
		if (item == null || item.getUserRef() == null) return;

		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

		if (firebaseUser == null) return;

		if (firebaseUser.getUid().equals(item.getUserRef().getId())) {
			imgBtn.setVisibility(View.VISIBLE);
		}

	}
}
