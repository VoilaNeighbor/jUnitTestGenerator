package com.seideun.java.test.generator.symbolic_executor;

import com.seideun.java.test.generator.constriant_solver.SootAgent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import soot.Local;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;
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

		// Todo(Seideun): Use some better coverage-checking tools.

		assertEquals(2, results.size());
		var path1 = false;
		var path2 = false;
		var jParameter = (Local) graph.getBody().getParameterLocal(0);
		for (Map<Local, Object> concreteValues: results) {
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

	@Test
	void solveManyIf() {
		var graph = makeGraph("manyIfs");

		var results = jcm.run(graph);

		assertEquals(3, results.size());
		var path1 = false;
		var path2 = false;
		var path3 = false;
		var jParameters = graph.getBody().getParameterLocals();
		for (Map<Local, Object> concreteValues: results) {
			var a = (int) concreteValues.get(jParameters.get(0));
			var b = (int) concreteValues.get(jParameters.get(1));
			if (a < b) {
				path1 = true;
			} else if (a == b + 2) {
				path2 = true;
			} else {
				path3 = true;
			}
		}
		assertTrue(path1);
		assertTrue(path2);
		assertTrue(path3);
	}


	@Test
	void solveArrays() {
		var graph = makeGraph("arrayAssign");

		var results = jcm.run(graph);
	}

	@Test
	void solveStringEquals() {
		var graph = makeGraph("stringEquals");

		var results = jcm.run(graph);

		assertEquals(2, results.size());
		var path1 = false;
		var path2 = false;
		var jParameters = graph.getBody().getParameterLocals();
		for (Map<Local, Object> concreteValues: results) {
			var a = (String) concreteValues.get(jParameters.get(0));
			var b = (String) concreteValues.get(jParameters.get(1));
			if (a.equals(b)) {
				path1 = true;
			} else {
				path2 = true;
			}
		}
		assertTrue(path1);
		assertTrue(path2);
	}

	@Test
	void solveLoop(){
		var graph = makeGraph("whileLoop");

		var results = jcm.run(graph);

		assertEquals(2, results.size());
		var path1 = false;
		var path2 = false;
		var jParameters = graph.getBody().getParameterLocals();
		for (Map<Local, Object> concreteValues: results) {
			var a = (int) concreteValues.get(jParameters.get(0));
			if (a > 10) {
				path1 = true;
			} else {
				path2 = true;
			}
		}
		assertTrue(path1);
		assertTrue(path2);
	}

	@Test
	void solveDouble() {
		var graph = makeGraph("arrayAssign");

		var results = jcm.run(graph);
	}

	@Test
	@Disabled
	void solveDependentLoops() {
		var graph = makeGraph("twoWhileLoops");

		var primePaths = findPrimePaths(graph);
		System.out.println(primePaths);

		var results = jcm.run(graph);

		System.out.println(results);
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