package com.seideun.java.test.generator.junit;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JUnitTestGeneratorTest {
	final JUnitTestGenerator generator = new JUnitTestGenerator();

	@Test
	void generateConsistencyTestForConstantFunctions() {
		assertEquals(
			"@Test void fnReturnsSameValue(){var first=obj.fn();var second=obj.fn();assertEquals(first,second);}",
			generator.generateForMethod("obj", "fn", new ArrayList<>())
		);
		assertEquals(
			"@Test void methodReturnsSameValue(){var first=myVar.method();var second=myVar.method();assertEquals(first,second);}",
			generator.generateForMethod("myVar", "method", new ArrayList<>())
		);
	}
}
