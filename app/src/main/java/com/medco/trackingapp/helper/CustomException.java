package com.medco.trackingapp.helper;

public class CustomException extends RuntimeException {

	public CustomException() {
		super();
	}

	public CustomException(String message, Throwable cause) {
		super(message, cause);

	}
}
