package com.seideun.java.test.generator.symbolic_executor;

import com.seideun.java.test.generator.constriant_solver.SootAgent;
import org.junit.jupiter.api.Test;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;

import java.util.Collection;
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
	void collectAllInputSymbols() {
		var graph = makeGraph("intSequential");
		var allParameters = graph.getBody().getParameterLocals();

		var paths = jcm.run(graph);

		var concreteValues = paths.get(0).keySet();
		assertTrue(setEqual(allParameters, concreteValues));
	}

	@Test
	void collectStringSymbols() {
		var graph = makeGraph("stringType");

		var paths = jcm.run(graph);

		for (var concreteValue: paths.get(0).values()) {
			assertTrue(concreteValue instanceof String);
		}
	}

	// I don't know how to refactor it yet. Let's add some constraints and see
	// how the logic goes.
	@Test
	void solveUnboundedInt() {
		var graph = makeGraph("intSequential");

		var arguments = jcm.run(graph).get(0);

		assertEquals(2, arguments.size());
		for (Object x: arguments.values()) {
			assertEquals(Integer.class, x.getClass());
		}
	}

	@Test
	void solveBoundedInt() {
		var graph = makeGraph("twoBranches");

		var results = jcm.run(graph);

		assertEquals(2, results.size());
		var path1 = false;
		var path2 = false;
		var jParameter = (JimpleLocal)graph.getBody().getParameterLocal(0);
		for (Map<JimpleLocal, Object> concreteValues: results) {
			var value = (int) concreteValues.get(jParameter);
			if (value < 2) {
				path1 = true;
			} else {
				path2 = true;
			}
		}
		assertTrue(path1);
		assertTrue(path2);
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