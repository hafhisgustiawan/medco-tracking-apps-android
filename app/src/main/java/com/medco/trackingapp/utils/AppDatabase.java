package com.medco.trackingapp.utils;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.medco.trackingapp.model.Well;
import com.medco.trackingapp.service.WellDAO;

@Database(entities = {Well.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
	public abstract WellDAO wellDAO();
}
