package com.seideun.java.test.generator.junit;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A text-generating class creating JUnit test for based on input.
 */
public class JUnitTestGenerator {

	public String generateForMethod(
		String objectName,
		String methodName,
		Collection<TestCase> testSuite
	) {
		if (testSuite.isEmpty()) {
			return "@Test void " + methodName + "ReturnsSameValue(){var first=" +
				objectName + "." + methodName + "();var second=" + objectName + "." +
				methodName + "();assertEquals(first,second);}";
		} else {
			var result = new StringBuilder();
			result.append("@Test void ")
				.append(methodName)
				.append("ReturnsAsExpected(){");
			for (var testCase: testSuite) {
				result.append("assertEquals(")
					.append(toLiteral(testCase.result())).append(",")
					.append(objectName).append(".")
					.append(methodName).append("(")
					.append(testCase.arguments().stream()
						.map(JUnitTestGenerator::toLiteral)
						.collect(Collectors.joining(","))
					).append("));");
			}
			return result.toString();
		}
	}

	private static String toLiteral(Object x) {
		if (x instanceof String s) {
			return '"' + s + '"';
		} else {
			return x.toString();
		}
	}
}
