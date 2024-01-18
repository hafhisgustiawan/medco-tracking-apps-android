package com.medco.trackingapp.model;

import android.net.Uri;

public class ImageItem {
	private Uri uri;
	private String image;
	private String type; //well | report

	public ImageItem() {
	}

	public ImageItem(Uri uri, String image) {
		this.uri = uri;
		this.image = image;
	}


	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
