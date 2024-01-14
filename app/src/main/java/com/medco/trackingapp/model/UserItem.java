package com.medco.trackingapp.model;

import com.google.firebase.Timestamp;

public class UserItem {
	private String email;
	private String name;
	private String photo;
	private String role;
	private String phone;
	private String uidDevice;
	private Timestamp timeRegister;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getUidDevice() {
		return uidDevice;
	}

	public void setUidDevice(String uidDevice) {
		this.uidDevice = uidDevice;
	}

	public Timestamp getTimeRegister() {
		return timeRegister;
	}

	public void setTimeRegister(Timestamp timeRegister) {
		this.timeRegister = timeRegister;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
}
