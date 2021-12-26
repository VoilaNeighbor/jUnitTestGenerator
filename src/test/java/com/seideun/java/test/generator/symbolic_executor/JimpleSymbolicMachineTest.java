package com.seideun.java.test.generator.symbolic_executor;

import com.seideun.java.test.generator.constriant_solver.SootAgent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JimpleSymbolicMachineTest {
	JimpleSymbolicMachine jsm = new JimpleSymbolicMachine();

	@Test
	void emptyJProgramReturnsEmptySymbolTable() {
		var empty = SootAgent.basicExamples.makeGraph("empty");

		jsm.run(empty);

		var state = jsm.state();
		assertTrue(state.symbolTable().isEmpty());
	}
}