package com.seideun.java.test.generator.constriant_solver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestCaseInputGeneratorTest {
	@Test
	void argumentsCannotHaveDuplicateNames() {
		assertThrows(
			TestCaseInputBuilder.ArgNameDuplicatedException.class,
			() -> new TestCaseInputBuilder()
				.addArgument(arbitraryClass(), "a")
				.addArgument(arbitraryClass(), "a")
		);
	}

	@Test
	void generateArbitraryInputForNoConstraints() {
		// Note that only a few types are known to TestCaseBuilder.
		var expectedOutput = arbitraryInt();
		var arguments = new TestCaseInputBuilder()
			.addArgument(Integer.class, "a")
			.addArgument(String.class, "b")
			.build();
		assertEquals(2, arguments.size());
		assertEquals(Integer.class, arguments.get(0).getClass());
		assertEquals(String.class, arguments.get(1).getClass());
	}


	private static int arbitraryInt() {
		return 9;
	}

	private static Class<?> arbitraryClass() {
		return Integer.class;
	}
}
