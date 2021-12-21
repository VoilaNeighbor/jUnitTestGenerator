package com.seideun.java.test.generator.constriant_solver;

public class TodoException extends RuntimeException {
	public TodoException() {
		super("Todo");
	}

	public TodoException(Object todo) {
		super("<Todo>" + todo + "</Todo>");
	}
}
