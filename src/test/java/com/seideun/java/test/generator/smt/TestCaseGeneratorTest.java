package com.seideun.java.test.generator.smt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestCaseGeneratorTest {
	@Test
	void throwsIfResultNotSet() {
		assertThrows(
			TestCaseBuilder.OutputNotSetException.class,
			() -> new TestCaseBuilder().build()
		);
	}

	@Test
	void argumentsCannotHaveDuplicateNames() {
		assertThrows(
			TestCaseBuilder.ArgNameDuplicatedException.class,
			() -> new TestCaseBuilder()
				.addArgument(arbitraryClass(), "a")
				.addArgument(arbitraryClass(), "a")
		);
	}

	@Test
	void generateArbitraryInputForNoConstraints() {
		// Note that only a few types are known to TestCaseBuilder.
		var expectedOutput = arbitraryInt();
		var testCase = new TestCaseBuilder()
			.addArgument(Integer.class, "a")
			.addArgument(String.class, "b")
			.setExpectedOutput(expectedOutput)
			.build()
			.result();
		assertEquals(expectedOutput, testCase.expectedOutput());
		assertEquals(2, testCase.arguments().size());
		assertEquals(Integer.class, testCase.arguments().get(0).getClass());
		assertEquals(String.class, testCase.arguments().get(1).getClass());
	}


	private static int arbitraryInt() {
		return 9;
	}

	private static Class<?> arbitraryClass() {
		return Integer.class;
	}
}
