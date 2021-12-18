package com.seideun.java.test_generator;

import java.util.List;

public class TestCase {
	public final List<Object> arguments;
	public final Object expectedOutput;

	public TestCase(List<Object> arguments, Object expectedOutput) {
		this.arguments = arguments;
		this.expectedOutput = expectedOutput;
	}
}
