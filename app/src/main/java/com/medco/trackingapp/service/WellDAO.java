package com.medco.trackingapp.service;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.medco.trackingapp.model.Well;

import java.util.List;

@Dao
public interface WellDAO {
	@Insert
	void insertAllWell(List<Well> wells);

	@Query("DELETE FROM well")
	void deleteAllWells();

	@Query("SELECT * FROM well")
	List<Well> getAllWell();

	@Query("SELECT * FROM well WHERE name LIKE '%' || :keyword || '%' ORDER BY name ASC")
	List<Well> getWellByKeyword(String keyword);
}
