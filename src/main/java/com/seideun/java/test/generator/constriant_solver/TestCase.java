package com.seideun.java.test.generator.constriant_solver;

import java.util.List;

public class TestCase {
	public final List<Object> arguments;
	public final Object expectedOutput;

	public TestCase(List<Object> arguments, Object expectedOutput) {
		this.arguments = arguments;
		this.expectedOutput = expectedOutput;
	}
}
