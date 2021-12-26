package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.Expr;
import com.seideun.java.test.generator.constriant_solver.SootAgent;
import org.junit.jupiter.api.Test;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

		var paths = jsm.run(graph);

		assertEquals(1, paths.size());
		assertTrue(paths.get(0).isEmpty());
	}

	@Test
	void walkAllBranches() {
		var graph = makeGraph("twoBranches");

		var paths = jsm.run(graph);

		assertEquals(2, paths.size());
	}

	@Test
	void collectAllSymbols() {
		var graph = makeGraph("intSequential");
		var allJVars = graph.getBody().getLocals();

		var paths = jsm.run(graph);

		var jVarsFound = paths.get(0).keySet();
		assertTrue(setEqual(allJVars, jVarsFound));
	}

	@Test
	void collectStringSymbols() {
		var graph = makeGraph("stringType");

		var paths = jsm.run(graph);

		for (var symbol: paths.get(0).values()) {
			assertEquals("String", symbol.getSort().toString());
		}
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