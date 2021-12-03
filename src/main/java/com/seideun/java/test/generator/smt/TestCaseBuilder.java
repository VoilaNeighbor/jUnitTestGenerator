package com.seideun.java.test.generator.smt;

import java.util.ArrayList;
import java.util.List;

/**
 * This class has not idea of a method or anything alike. It accepts a flock of
 * typed constraints and an expected result, and then generate a conforming
 * test case.
 */
public class TestCaseBuilder {
	private final List<Class<?>> argumentTypes;
	private Object expectedOutput;
	private TestCase result;

	public TestCaseBuilder() {
		argumentTypes = new ArrayList<>();
	}

	public TestCaseBuilder addArgument(Class<?> type) {
		argumentTypes.add(type);
		return this;
	}

	public TestCaseBuilder setExpectedOutput(Object expectedOutput) {
		this.expectedOutput = expectedOutput;
		return this;
	}

	public TestCaseBuilder build() {
		if (expectedOutput == null) {
			throw new OutputNotSetException();
		}
		result = new TestCase(makeArguments(), expectedOutput);
		return this;
	}

	public TestCase result() {
		return result;
	}

	private List<Object> makeArguments() {
		return argumentTypes.stream()
			.map(TestCaseBuilder::makeDefaultObject)
			.toList();
	}

	private static Object makeDefaultObject(Class<?> theClass) {
		if (theClass == Integer.class) {
			return 0;
		} else {
			return "";
		}
	}

	/**
	 * There must be an output for a test case.
	 */
	public static class OutputNotSetException extends RuntimeException {
	}
}
