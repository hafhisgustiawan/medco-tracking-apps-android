package com.medco.trackingapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "well")
public class Well {
	@ColumnInfo(name = "well_id")
	@PrimaryKey(autoGenerate = true)
	public int uid;

	@ColumnInfo(name = "name")
	public String name;

	@ColumnInfo(name = "docId")
	public String docId;

	@Ignore
	public Well() {

	}

	public Well(String name, String docId) {
		this.uid = 0;
		this.name = name;
		this.docId = docId;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}
}
