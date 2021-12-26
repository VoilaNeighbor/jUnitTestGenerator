package com.seideun.java.test.generator.constriant_solver;

import static java.lang.String.format;

public class TodoException extends RuntimeException {
	public TodoException() {
		super("Todo");
	}

	public TodoException(Object todo) {
		super(format("<Todo class=\"%s\">%s</Todo>", todo.getClass(), todo));
	}
}
