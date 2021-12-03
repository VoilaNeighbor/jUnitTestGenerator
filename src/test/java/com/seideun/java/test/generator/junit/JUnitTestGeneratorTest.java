package com.seideun.java.test.generator.junit;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JUnitTestGeneratorTest {
	final JUnitTestGenerator generator = new JUnitTestGenerator();

	@Test
	void generateConsistencyTestForConstantFunctions() {
		generator.setObjectName("myVar");
		generator.setMethodName("myMethod");
		assertEquals(
			"@Test void myMethodReturnsSameValue(){" +
				"var first=myVar.myMethod();" +
				"var second=myVar.myMethod();" +
				"assertEquals(first,second);}",
			generator.generateForMethod()
		);

		generator.setObjectName("anotherVar");
		generator.setMethodName("anotherMethod");
		assertEquals(
			"@Test void anotherMethodReturnsSameValue(){" +
				"var first=anotherVar.anotherMethod();" +
				"var second=anotherVar.anotherMethod();" +
				"assertEquals(first,second);}",
			generator.generateForMethod()
		);
	}

	@Test
	void generateTestForEachInputSuite() {
		generator.setObjectName("myVar");
		generator.setMethodName("myMethod");
		assertEquals(
			"@Test void myMethodReturnsAsExpected(){" +
				"assertEquals(0,myVar.myMethod(1,2));" +
				"assertEquals(2,myVar.myMethod(0,1));",
			generator.generateForMethod(List.of(
				new TestCase(List.of(1, 2), 0),
				new TestCase(List.of(0, 1), 2)
			))
		);
		generator.setObjectName("anotherVar");
		generator.setMethodName("anotherMethod");
		assertEquals(
			"@Test void anotherMethodReturnsAsExpected(){" +
				"assertEquals(\"c\",anotherVar.anotherMethod(\"a\",\"b\"));" +
				"assertEquals(\"r\",anotherVar.anotherMethod(\"p\",\"q\"));",
			generator.generateForMethod(List.of(
				new TestCase(List.of("a", "b"), "c"),
				new TestCase(List.of("p", "q"), "r")
			))
		);
	}

	// inconsistentNumOfArgumentsThrow
}
