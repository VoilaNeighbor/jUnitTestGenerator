package com.seideun.java.test.generator.constriant_solver;

import lombok.SneakyThrows;

import java.io.Writer;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * A text-generating class creating JUnit test for based on input.
 */
public class JUnitTestGenerator {
	private final StringBuilder builder = new StringBuilder();
	private final String objectName;
	private final String methodName;

	// aka "objectName.methodName"
	private String methodAccess;

	public JUnitTestGenerator(String objectName, String methodName) {
		this.objectName = objectName;
		this.methodName = methodName;
	}

	public JUnitTestGenerator addTestMethod(String methodName, Collection<TestCase> testSuite) {
		if (testSuite.isEmpty()) {
			throw new NoTestCaseException();
		}
		makeMethodAccessExpression(methodName);
		builder.append(format("@Test void %s(){", methodName));
		testSuite.forEach(this::buildAssertion);
		builder.append("}");
		return this;
	}

	@SneakyThrows
	public void buildToWriter(Writer out) {
		var prepend = """
				package generated_test;
				import static org.junit.jupiter.api.Assertions.assertEquals;
				import static org.junit.jupiter.api.Assertions.assertTrue;

				class MyTest {""";
		var result = prepend + builder + "}";
		out.append(result);
	}

	@Deprecated
	@SneakyThrows
	public String generateAssertForEachCase(
			Collection<TestCase> testSuite,
			Writer out
	) {
		var prepend = """
				package generated_test;
				import static org.junit.jupiter.api.Assertions.assertEquals;
				import static org.junit.jupiter.api.Assertions.assertTrue;

				class MyTest {""";
		var result = prepend + generateAssertForEachCase(testSuite) + "}";
		out.append(result);
		return result;
	}

	public String generateAssertForEachCase(Collection<TestCase> testSuite) {
		if (testSuite.isEmpty()) {
			throw new NoTestCaseException();
		}
		makeMethodAccessExpression(methodName);
		builder.setLength(0);
		builder.append(format("@Test void %sReturnsAsExpected(){", methodName));
		testSuite.forEach(this::buildAssertion);
		builder.append("}");
		return builder.toString();
	}

	private void makeMethodAccessExpression(String methodName) {
		methodAccess = objectName + '.' + methodName;
	}

	private void buildAssertion(TestCase testCase) {
		if (testCase.expectedOutput == null) {
			builder.append(format(
					"assertDoesNotThrow(()->%s(%s));",
					methodAccess, makeArgumentLiterals(testCase.arguments)
			));
		} else {
			builder.append(format(
					"assertEquals(%s,%s(%s));",
					toLiteral(testCase.expectedOutput),
					methodAccess,
					makeArgumentLiterals(testCase.arguments)
			));
		}
	}

	/**
	 * Converts an object to a Java-source-code-conforming literal.
	 */
	private static String toLiteral(Object x) {
		if (x instanceof String) {
			String s = (String) x;
			// Currently, only String is specially treated.
			return '"' + s + '"';
		} else if (x instanceof Object[] arr) {
			// todo
			return null;
		} else {
			return Objects.toString(x);
		}
	}

	private static String makeArgumentLiterals(Collection<Object> arguments) {
		return arguments.stream()
				.map(JUnitTestGenerator::toLiteral)
				.collect(Collectors.joining(","));
	}

	public static class NoTestCaseException extends RuntimeException {
	}
}
