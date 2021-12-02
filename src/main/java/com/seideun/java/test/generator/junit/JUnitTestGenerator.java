package com.seideun.java.test.generator.junit;

import java.util.List;

/**
 * Generate JUnit test for pure methods.
 */
public class JUnitTestGenerator {

	public String generateForMethod(
		String objectName,
		String methodName,
		List<MethodArgument> inputSuite
	) {
		return "@Test void " + methodName + "ReturnsSameValue(){var first=" +
			objectName + "." + methodName + "();var second=" + objectName + "." +
			methodName + "();assertEquals(first,second);}";
	}
}
