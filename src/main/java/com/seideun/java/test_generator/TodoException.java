package com.seideun.java.test_generator;

public class TodoException extends RuntimeException {
	public TodoException() {
		super("Todo");
	}

	public TodoException(Object todo) {
		super("<Todo>" + todo + "</Todo>");
	}
}
