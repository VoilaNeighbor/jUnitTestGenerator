package com.seideun.java.test.generator.constriant_solver;

import lombok.SneakyThrows;

import java.io.Writer;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * A text-generating class creating JUnit test for based on input.
 */
public class JUnitTestGenerator {
	private static final int ARBITRARY_CAPACITY = 100;
	private final StringBuilder builder = new StringBuilder(ARBITRARY_CAPACITY);
	private final String objectName;
	private final String methodName;

	// aka "objectName.methodName"
	private String methodAccess;

	public JUnitTestGenerator(String objectName, String methodName) {
		this.objectName = objectName;
		this.methodName = methodName;
	}

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
		makeMethodAccessExpression();
		builder.setLength(0);
		builder.append(format("@Test void %sReturnsAsExpected(){", methodName));
		testSuite.forEach(this::buildAssertion);
		builder.append("}");
		return builder.toString();
	}

	private void makeMethodAccessExpression() {
		methodAccess = objectName + '.' + methodName;
	}

	private void buildAssertion(TestCase testCase) {
		builder.append(format(
			"assertEquals(%s,%s(%s));",
			toLiteral(testCase.expectedOutput),
			methodAccess,
			makeArgumentLiterals(testCase.arguments)
		));
	}

	/**
	 * Converts an object to a Java-source-code-conforming literal.
	 */
	private static String toLiteral(Object x) {
		if (x instanceof String) {
			String s = (String) x;
			// Currently, only String is specially treated.
			return '"' + s + '"';
		} else {
			return x.toString();
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
