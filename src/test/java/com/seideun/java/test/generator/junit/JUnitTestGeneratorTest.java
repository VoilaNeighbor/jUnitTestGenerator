package com.seideun.java.test.generator.junit;

import com.seideun.java.test.generator.smt.TestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JUnitTestGeneratorTest {
	// Note(Seideun): I think of refactoring away the duplication, but I'm somehow
	// 	lazy for this now.
	@Test
	void generateConsistencyTestForConstantFunctions() {
		var generator = new JUnitTestGenerator("myVar", "myMethod");
		assertEquals(
			"@Test void myMethodReturnsSameValue(){" +
				"var first=myVar.myMethod();" +
				"var second=myVar.myMethod();" +
				"assertEquals(first,second);}",
			generator.generateConsistencyTest()
		);

		generator = new JUnitTestGenerator("anotherVar", "anotherMethod");
		assertEquals(
			"@Test void anotherMethodReturnsSameValue(){" +
				"var first=anotherVar.anotherMethod();" +
				"var second=anotherVar.anotherMethod();" +
				"assertEquals(first,second);}",
			generator.generateConsistencyTest()
		);
	}

	@Test
	void generateTestForEachInputSuite() {
		var generator = new JUnitTestGenerator("myVar", "myMethod");
		assertEquals(
			"@Test void myMethodReturnsAsExpected(){" +
				"assertEquals(0,myVar.myMethod(1,2));" +
				"assertEquals(2,myVar.myMethod(0,1));",
			generator.generateAssertForEachCase(List.of(
				new TestCase(List.of(1, 2), 0),
				new TestCase(List.of(0, 1), 2)
			))
		);

		generator = new JUnitTestGenerator("anotherVar", "anotherMethod");
		assertEquals(
			"@Test void anotherMethodReturnsAsExpected(){" +
				"assertEquals(\"c\",anotherVar.anotherMethod(\"a\",\"b\"));" +
				"assertEquals(\"r\",anotherVar.anotherMethod(\"p\",\"q\"));",
			generator.generateAssertForEachCase(List.of(
				new TestCase(List.of("a", "b"), "c"),
				new TestCase(List.of("p", "q"), "r")
			))
		);
	}
}
