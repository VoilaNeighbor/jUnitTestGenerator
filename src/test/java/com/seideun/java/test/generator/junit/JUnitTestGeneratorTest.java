package com.seideun.java.test.generator.junit;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JUnitTestGeneratorTest {
	final JUnitTestGenerator generator = new JUnitTestGenerator();

	@Test
	void generateConsistencyTestForConstantFunctions() {
		assertEquals(
			"@Test void fnReturnsSameValue(){var first=obj.fn();var second=obj.fn" +
				"()" +
				";" +
				"assertEquals(first,second);}",
			generator.generateForMethod("obj", "fn", new ArrayList<>())
		);
		assertEquals(
			"@Test void methodReturnsSameValue(){var first=myVar.method();var " +
				"second=myVar.method();assertEquals(first,second);}",
			generator.generateForMethod("myVar", "method", new ArrayList<>())
		);
	}

	@Test
	void generateTestForEachInputSuite() {
		assertEquals(
			"@Test void fnReturnsAsExpected(){assertEquals(0,obj.fn(1,2));" +
				"assertEquals(2,obj.fn(0,1));",
			generator.generateForMethod("obj", "fn", List.of(
				new TestCase(List.of(1, 2), 0),
				new TestCase(List.of(0, 1), 2)
			))
		);
		assertEquals(
			"@Test void myFnReturnsAsExpected(){assertEquals(\"c\",myVar.myFn" +
				"(\"a\",\"b\"));assertEquals(\"r\",myVar.myFn(\"p\",\"q\"));",
			generator.generateForMethod("myVar", "myFn", List.of(
				new TestCase(List.of("a", "b"), "c"),
				new TestCase(List.of("p", "q"), "r")
			))
		);
	}

	// inconsistentNumOfArgumentsThrow
}
