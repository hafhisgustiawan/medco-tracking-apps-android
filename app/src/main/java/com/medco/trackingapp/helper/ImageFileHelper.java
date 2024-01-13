package com.medco.trackingapp.helper;

import android.annotation.SuppressLint;
import android.os.Environment;

import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageFileHelper {
	public FragmentActivity mActivity;

	public ImageFileHelper(FragmentActivity activity) {
		this.mActivity = activity;
	}

	public File createImageFile() throws IOException {
		// Create an image file name
		@SuppressLint("SimpleDateFormat")
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

		return File.createTempFile(
			imageFileName,  /* prefix */
			".jpg",         /* suffix */
			storageDir      /* directory */
		);
	}
}
