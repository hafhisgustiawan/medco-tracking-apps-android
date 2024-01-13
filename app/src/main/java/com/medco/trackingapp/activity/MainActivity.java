package com.medco.trackingapp.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.medco.trackingapp.R;

public class MainActivity extends AppCompatActivity {

	public static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
}