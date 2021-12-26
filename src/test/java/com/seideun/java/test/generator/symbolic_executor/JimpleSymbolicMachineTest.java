package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.Expr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Status;
import com.seideun.java.test.generator.constriant_solver.SootAgent;
import com.seideun.java.test.generator.constriant_solver.TodoException;
import org.junit.jupiter.api.Test;
import soot.Local;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

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
			symbol-renaming
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
		var graph = makeGraph("intSequential");

		var symbolTable = jsm.run(graph).get(0);

		var solver = jsm.z3.mkSolver();
		var status = solver.check();
		assertEquals(Status.SATISFIABLE, status);
		var model = solver.getModel();

		var parameters = graph.getBody().getParameterLocals();
		var concreteValues = new HashMap<Local, Object>();
		for (Local parameter: parameters) {
			var symbolicValue = symbolTable.get(parameter);
			var interpretation = model.eval(symbolicValue, true);
			if (interpretation instanceof IntNum x) {
				concreteValues.put(parameter, x.getInt());
			} else {
				throw new TodoException(interpretation);
			}
		}

		assertEquals(2, concreteValues.size());
		for (Object x: concreteValues.values()) {
			assertEquals(Integer.class, x.getClass());
		}
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