package com.seideun.java.test.generator.smt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
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

	public TestCaseBuilder addArgument(Class<Integer> type) {
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
		result = new TestCase(
			argumentTypes.stream()
				.map(TestCaseBuilder::makeDefaultObject)
				.toList(),
			expectedOutput
		);
		return this;
	}

	public TestCase result() {
		return result;
	}

	private static Object makeDefaultObject(Class<?> theClass) {
		return 0;
	}

	/**
	 * There must be an output for a test case.
	 */
	public static class OutputNotSetException extends RuntimeException {
	}
}
