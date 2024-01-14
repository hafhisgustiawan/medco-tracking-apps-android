package com.medco.trackingapp.service;

import com.medco.trackingapp.model.ResultMapsDistanceItem;
import com.medco.trackingapp.model.ResultMapsPlaceItem;
import com.medco.trackingapp.model.direction.DirectionResponse;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapsApiInterface {

	@GET("maps/api/directions/json")
	Single<DirectionResponse> getDirection(@Query("mode") String mode,
//                                           @Query("transit_routing_preference") String preference,
																				 @Query("alternatives") Boolean alt,
																				 @Query("origin") String origin,
																				 @Query("destination") String destination,
																				 @Query("key") String key);

	@GET("maps/api/distancematrix/json")
	Single<ResultMapsDistanceItem> getDistance(@Query("key") String key,
																						 @Query("origins") String origin,
																						 @Query("destinations") String destination);

	@GET("maps/api/place/queryautocomplete/json")
	Call<ResultMapsPlaceItem> getPlace(@Query("input") String text, @Query("key") String key);
}
