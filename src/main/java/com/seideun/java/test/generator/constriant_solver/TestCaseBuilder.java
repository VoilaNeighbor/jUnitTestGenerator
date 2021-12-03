package com.seideun.java.test.generator.constriant_solver;

import java.util.ArrayList;
import java.util.List;

/**
 * This class has not idea of a method or anything alike. It accepts a flock of
 * typed constraints and an expected result, and then generate a conforming
 * test case.
 */
public class TestCaseBuilder {
	/**
	 * There must be an output for a test case.
	 */
	public static class OutputNotSetException extends RuntimeException {
	}

	public static class ArgNameDuplicatedException extends RuntimeException {
	}

	private record Argument(Class<?> type, String name) {}

	private final List<Argument> arguments;
	private Object expectedOutput;
	private TestCase result;

	public TestCaseBuilder() {
		arguments = new ArrayList<>();
	}

	public TestCaseBuilder addArgument(Class<?> type, String name) {
		if (arguments.stream().anyMatch(x -> name.equals(x.name))) {
			throw new ArgNameDuplicatedException();
		}
		arguments.add(new Argument(type, name));
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
		return arguments.stream()
			.map(x -> makeDefaultObject(x.type))
			.toList();
	}

	private static Object makeDefaultObject(Class<?> theClass) {
		if (theClass == Integer.class) {
			return 0;
		} else {
			return "";
		}
	}
}
