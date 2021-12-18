package com.seideun.java.test_generator;

import soot.Unit;

import java.util.List;

import static java.lang.String.format;

public class StrangeArgumentOrderException extends RuntimeException {
	public StrangeArgumentOrderException(List<Unit> path, int expectedArgIndex) {
		super(format(
			"<StrangeArgumentOrderException>Strange argument order in path: " +
				"%s, was expecting %s</StrangeArgumentOrderException>",
			path, expectedArgIndex
		));
	}
}
