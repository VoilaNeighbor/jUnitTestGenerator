package com.seideun.java.test.generator.common;

import com.seideun.java.test.generator.examples.JcmExamples;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExampleTest {
	JcmExamples jcmExamples = new JcmExamples();

	@Test
	void empty() {
		assertDoesNotThrow(() -> jcmExamples.empty());
	}

	@Test
	void intSequential() {
		assertEquals(0, jcmExamples.intSequential(0, 0));
	}

	@Test
	void twoBranches() {
		assertEquals(0, jcmExamples.twoBranches(0));
		assertEquals(1, jcmExamples.twoBranches(2));
	}

	@Test
	void manyIfs() {
		assertEquals(1, jcmExamples.manyIfs(0, 1));
		assertEquals(2, jcmExamples.manyIfs(2, 0));
		assertEquals(0, jcmExamples.manyIfs(1, 0));
	}

	@Test
	void whileLoop() {
		assertEquals(9, jcmExamples.whileLoop(11));
		assertEquals(0, jcmExamples.whileLoop(0));
	}

	@Test
	void twoWhileLoops() {
		assertEquals(9, jcmExamples.twoWhileLoops(12));
		assertEquals(7, jcmExamples.twoWhileLoops(0));
		assertEquals(7, jcmExamples.twoWhileLoops(6));
	}

	@Test
	void stringType() {
		assertEquals("Hello", jcmExamples.stringType(""));
	}

	@Test
	void stringEquals() {
		assertEquals("!0!!0!", jcmExamples.stringEquals("!0!", "!0!"));
		assertEquals("!0!", jcmExamples.stringEquals("!0!", "!1!"));
	}

	@Test
	void arrayType() {
		assertThrows(Exception.class, () -> jcmExamples.arrayType(new int[0]));
	}

	@Test
	void arrayAssign() {
		assertDoesNotThrow(() -> jcmExamples.arrayAssign(new int[]{3, 0}, 0));
	}
}