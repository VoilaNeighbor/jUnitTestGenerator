package com.seideun.java.test.generator.junit;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A text-generating class creating JUnit test for based on input.
 */
@NotThreadSafe
public class JUnitTestGenerator {
	private static final int ARBITRARY_CAPACITY = 100;
	private final StringBuilder builder = new StringBuilder(ARBITRARY_CAPACITY);
	private String objectName;
	private String methodName;

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String generateForMethod(
		Collection<TestCase> testSuite
	) {
		if (testSuite.isEmpty()) {
			return "@Test void " + methodName + "ReturnsSameValue(){var first=" +
				objectName + "." + methodName + "();var second=" + objectName + "." +
				methodName + "();assertEquals(first,second);}";
		} else {
			builder.setLength(0);
			builder.append("@Test void ")
				.append(methodName)
				.append("ReturnsAsExpected(){");
			for (var testCase: testSuite) {
				buildAssertion(objectName, methodName, testCase);
			}
			return builder.toString();
		}
	}

	private void buildAssertion(
		String objectName,
		String methodName,
		TestCase testCase
	) {
		builder.append("assertEquals(")
			.append(toLiteral(testCase.result())).append(",")
			.append(objectName).append(".")
			.append(methodName).append("(")
			.append(makeArgumentLiterals(testCase.arguments()))
			.append("));");
	}


	private static String makeArgumentLiterals(Collection<Object> arguments) {
		return arguments.stream()
			.map(JUnitTestGenerator::toLiteral)
			.collect(Collectors.joining(","));
	}

	private static String toLiteral(Object x) {
		if (x instanceof String s) {
			return '"' + s + '"';
		} else {
			return x.toString();
		}
	}
}
