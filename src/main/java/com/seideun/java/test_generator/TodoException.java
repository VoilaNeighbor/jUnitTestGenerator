package com.seideun.java.test_generator;

public class TodoException extends RuntimeException {
	public TodoException() {
		super("Todo");
	}

	public TodoException(String todo) {
		super("Todo: " + todo);
	}
}
