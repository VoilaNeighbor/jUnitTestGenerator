package com.seideun.java.test.generator.smt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestCaseGeneratorTest {
	@Test
	void generateArbitraryInputForNoConstraints() {
		// Note that only a few types are known to TestCaseBuilder.
		var expectedOutput = arbitraryInt();
		var testCase = new TestCaseBuilder()
			.addArgument(Integer.class)
			.addArgument(String.class)
			.setExpectedOutput(expectedOutput)
			.build()
			.result();

		assertEquals(expectedOutput, testCase.expectedOutput());
		assertEquals(2, testCase.arguments().size());
		assertEquals(Integer.class, testCase.arguments().get(0).getClass());
		assertEquals(String.class, testCase.arguments().get(1).getClass());
	}

	@Test
	void throwsIfResultNotSet() {
		assertThrows(
			TestCaseBuilder.OutputNotSetException.class,
			() -> new TestCaseBuilder().build()
		);
	}

	private static int arbitraryInt() {
		return 9;
	}
}
