package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.IntNum;
import com.microsoft.z3.Status;
import com.seideun.java.test.generator.constriant_solver.SootAgent;
import com.seideun.java.test.generator.constriant_solver.TodoException;
import org.junit.jupiter.api.Test;
import soot.Local;
import soot.toolkits.graph.UnitGraph;

import java.util.Collection;
import java.util.HashMap;

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
	Concolic fuzz running:
		???
 */
class JimpleConcolicMachineTest {
	JimpleConcolicMachine jcm = new JimpleConcolicMachine();

	@Test
	void emptyJProgramReturnsEmptyPath() {
		var graph = makeGraph("empty");

		var paths = jcm.run(graph);

		assertEquals(1, paths.size());
		assertTrue(paths.get(0).isEmpty());
	}

	@Test
	void walkAllBranches() {
		var graph = makeGraph("twoBranches");

		var paths = jcm.run(graph);

		assertEquals(2, paths.size());
	}

	@Test
	void collectAllSymbols() {
		var graph = makeGraph("intSequential");
		var allJVars = graph.getBody().getLocals();

		var paths = jcm.run(graph);

		var jVarsFound = paths.get(0).keySet();
		assertTrue(setEqual(allJVars, jVarsFound));
	}

	@Test
	void collectStringSymbols() {
		var graph = makeGraph("stringType");

		var paths = jcm.run(graph);

		for (var symbol: paths.get(0).values()) {
			assertEquals("String", symbol.getSort().toString());
		}
	}

	// I don't know how to refactor it yet. Let's add some constraints and see
	// how the logic goes.
	@Test
	void solveUnboundedInt() {
		var graph = makeGraph("intSequential");

		var symbolTable = jcm.run(graph).get(0);

		var solver = jcm.z3.mkSolver();
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

	@Test
	void solveBoundedInt() {
		var graph = makeGraph("twoBranches");

	}

	private static UnitGraph makeGraph(String name) {
		return SootAgent.jcmExamples.makeGraph(name);
	}

	private static <T> boolean setEqual(
		Collection<? extends T> lhs,
		Collection<? extends T> rhs
	) {
		//noinspection SuspiciousMethodCalls
		return lhs.size() == rhs.size() && lhs.containsAll(rhs);
	}
}