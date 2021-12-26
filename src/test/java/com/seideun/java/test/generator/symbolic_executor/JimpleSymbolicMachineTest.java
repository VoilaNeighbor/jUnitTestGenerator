package com.seideun.java.test.generator.symbolic_executor;

import com.seideun.java.test.generator.constriant_solver.SootAgent;
import org.junit.jupiter.api.Test;
import soot.toolkits.graph.UnitGraph;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
	void emptyJProgramReturnsEmptyPath() {
		var graph = makeGraph("empty");

		jsm.run(graph);

		var paths = jsm.resultPaths();
		assertEquals(1, paths.size());
		assertTrue(paths.get(0).isEmpty());
	}

	@Test
	void collectAllSymbols() {
		var graph = makeGraph("intSequential");
		var allJVars = graph.getBody().getLocals();

		jsm.run(graph);

		var jVarsFound = jsm.resultPaths().get(0).keySet();
		assertTrue(setEqual(allJVars, jVarsFound));
	}

	@Test
	void collectStringSymbols() {
		var graph = makeGraph("stringType");

		jsm.run(graph);

		var path = jsm.resultPaths().get(0);
		for (var symbol: path.values()) {
			assertEquals("String", symbol.getSort().toString());
		}
	}

	@Test
	void solveBoundedInt() {
//		var graph = makeGraph("twoBranches");
//
//		jsm.run(graph);
	}

	@Test
	void solveUnboundedInt() {

	}

	private static UnitGraph makeGraph(String name) {
		return SootAgent.jsmExamples.makeGraph(name);
	}

	private static <T> boolean setEqual(
		Collection<? extends T> lhs,
		Collection<? extends T> rhs
	) {
		//noinspection SuspiciousMethodCalls
		return lhs.size() == rhs.size() && lhs.containsAll(rhs);
	}
}