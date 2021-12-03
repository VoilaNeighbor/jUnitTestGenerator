package com.seideun.java.test.generator.constriant_solver;

import java.util.ArrayList;
import java.util.List;

/**
 * This class has no idea of a method or anything alike. It accepts a flock of
 * typed constraints and then generate a conforming input set.
 */
public class TestCaseInputBuilder {
	public static class ArgNameDuplicatedException extends RuntimeException {
	}

	private record Argument(Class<?> type, String name) {}

	private final List<Argument> arguments;

	public TestCaseInputBuilder() {
		arguments = new ArrayList<>();
	}

	public TestCaseInputBuilder addArgument(Class<?> type, String name) {
		if (arguments.stream().anyMatch(x -> name.equals(x.name))) {
			throw new ArgNameDuplicatedException();
		}
		arguments.add(new Argument(type, name));
		return this;
	}

	public List<Object> build() {
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
