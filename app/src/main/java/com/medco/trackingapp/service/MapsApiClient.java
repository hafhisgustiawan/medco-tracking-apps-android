package com.medco.trackingapp.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsApiClient {

	public static final String BASE_URL = "https://maps.googleapis.com/";

	private static Retrofit retrofit = null;

	public static Retrofit getClient() {
		if (retrofit == null) {
			HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
			interceptor.level(HttpLoggingInterceptor.Level.BODY);
			OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.addInterceptor(interceptor)
				.build();

			Gson gson = new GsonBuilder()
				.setLenient()
				.create();

			retrofit = new Retrofit.Builder()
				.baseUrl(BASE_URL)
				.client(okHttpClient)
				.addConverterFactory(GsonConverterFactory.create(gson))
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.build();
		}
		return retrofit;
	}
}
