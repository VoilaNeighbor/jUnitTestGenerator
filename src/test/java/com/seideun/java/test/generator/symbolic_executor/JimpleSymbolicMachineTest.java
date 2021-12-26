package com.seideun.java.test.generator.symbolic_executor;

import com.seideun.java.test.generator.constriant_solver.SootAgent;
import org.junit.jupiter.api.Test;
import soot.toolkits.graph.UnitGraph;

import static org.junit.jupiter.api.Assertions.assertTrue;

/*
	It is hard to mock Jimple or Z3. Since these two friends are the de-facto
	standards in their fields for Java, we have no need to wrap them around
	anyway.

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
		var empty = makeGraph("empty");

		jsm.run(empty);

		var state = jsm.state();
		assertTrue(state.symbolTable().isEmpty());
	}

	@Test
	void collectAllSymbols() {
		var graph = makeGraph("intSequential");
		var expected = graph.getBody().getLocals();

		jsm.run(graph);

		var result = jsm.state().symbolTable().keySet();
		assertTrue(result.containsAll(expected));
	}

	@Test
	void solveBoundedInt() {

	}

	@Test
	void solveUnboundedInt() {

	}

	@Test
	void solveString() {

	}

	private static UnitGraph makeGraph(String name) {
		return SootAgent.jsmExamples.makeGraph(name);
	}

}