package com.seideun.java.test.generator.symbolic_executor;

import com.seideun.java.test.generator.constriant_solver.SootAgent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
	Data types:
		Primitive:
			Int
			Double & Float
		Composite:
			Array
			String
			Class
	Control flow:
		sequential program
		if-else
		loop:
			dynamic execution
	Con-colic fuzz running:
		???
 */
class JimpleSymbolicMachineTest {
	JimpleSymbolicMachine jsm = new JimpleSymbolicMachine();

	@Test
	void emptyJProgramReturnsEmptySymbolTable() {
		var empty = SootAgent.jsmExamples.makeGraph("empty");

		jsm.run(empty);

		var state = jsm.state();
		assertTrue(state.symbolTable().isEmpty());
	}

	@Test
	void intSequentialProgram() {

	}
}