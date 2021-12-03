package com.seideun.java.test.generator.junit;

import com.seideun.java.test.generator.smt.TestCase;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * A text-generating class creating JUnit test for based on input.
 */
@NotThreadSafe
public class JUnitTestGenerator {
	private static final int ARBITRARY_CAPACITY = 100;
	private final StringBuilder builder = new StringBuilder(ARBITRARY_CAPACITY);
	private String objectName;
	private String methodName;

	// aka "objectName.methodName"
	private String methodAccess;

	public JUnitTestGenerator() {
	}

	public JUnitTestGenerator(String objectName, String methodName) {
		this.objectName = objectName;
		this.methodName = methodName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * A no-arg method should return the same value when called multiple times.
	 */
	public String generateConsistencyTest() {
		makeMethodAccessExpression();
		return format(
			"@Test void %sReturnsSameValue(){" +
				"var first=%s();" + "var second=%s();" +
				"assertEquals(first,second);}",
			methodName, methodAccess, methodAccess
		);
	}

	public String generateAssertForEachCase(Collection<TestCase> testSuite) {
		if (testSuite.isEmpty()) {
			throw new NoTestCaseException();
		}
		builder.setLength(0);
		builder.append(format("@Test void %sReturnsAsExpected(){", methodName));
		testSuite.forEach(this::buildAssertion);
		return builder.toString();
	}

	private void buildAssertion(TestCase testCase) {
		builder.append(format(
			"assertEquals(%s,%s(%s));",
			toLiteral(testCase.expectedResult()),
			methodAccess,
			makeArgumentLiterals(testCase.arguments())
		));
	}

	private void makeMethodAccessExpression() {
		methodAccess = objectName + '.' + methodName;
	}

	private static String makeArgumentLiterals(Collection<Object> arguments) {
		return arguments.stream()
			.map(JUnitTestGenerator::toLiteral)
			.collect(Collectors.joining(","));
	}

	/**
	 * Converts an object to a Java-source-code-conforming literal.
	 */
	private static String toLiteral(Object x) {
		if (x instanceof String s) {
			// Currently, only String is specially treated.
			return '"' + s + '"';
		} else {
			return x.toString();
		}
	}

	public static class NoTestCaseException extends RuntimeException {
	}
}
