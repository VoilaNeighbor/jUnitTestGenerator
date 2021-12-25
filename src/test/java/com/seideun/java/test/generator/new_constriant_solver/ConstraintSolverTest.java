package com.seideun.java.test.generator.new_constriant_solver;

import com.microsoft.z3.Status;
import com.seideun.java.test.generator.new_constraint_solver.ConstraintSolver;
import org.junit.jupiter.api.Test;
import soot.IntType;
import soot.jimple.IntConstant;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;
import static com.seideun.java.test.generator.constriant_solver.SootAgent.exampleCfg;
import static org.junit.jupiter.api.Assertions.*;

class ConstraintSolverTest {
	static final UnitGraph exampleGraph = exampleCfg("twoArgs");
	ConstraintSolver solver = new ConstraintSolver();

	@Test
	void findAllInputSymbols() {
		var thePath = new ArrayList<>(findPrimePaths(exampleGraph).get(0));

		var result = solver.findAllInputSymbols(thePath);

		assertEquals(fakeFindInputSymbols(exampleGraph, 2), result);
	}

	@Test
	void makeArbitraryForUnboundedInput() {
		var inputSymbols = fakeFindInputSymbols(exampleGraph, 2);
		var thePath = new ArrayList<>(findPrimePaths(exampleGraph).get(0));

		var result = solver.solveSymbols(inputSymbols, thePath);

		assertEquals(Integer.class, result.get(0).getClass());
		assertEquals(Integer.class, result.get(1).getClass());
	}

	@Test
	void solveOneConstraint() {
		var symbol = new JimpleLocal("x", IntType.v());
		var conceivedConstraint = new JGeExpr(symbol, IntConstant.v(1));

		var result = solver.solveOneConstraint(symbol, conceivedConstraint);

		assertEquals(Status.SATISFIABLE, result.getRight());
		assertTrue((int)result.getLeft() >= 1);
	}

	@Test
	void reportUnsatisfiableConstraints() {
		var symbol = new JimpleLocal("x", IntType.v());
		var conceivedConstraint = new JGtExpr(symbol, symbol);

		var result = solver.solveOneConstraint(symbol, conceivedConstraint);

		assertNull(result.getLeft());
		assertEquals(Status.UNSATISFIABLE, result.getRight());
	}

	/**
	 * Jimple method body always put the input symbols at the start of a method.
	 * If we know what method we are testing against, we can extract them
	 * directly.
	 *
	 * @param graph   Example graph.
	 * @param numArgs Number of arguments we know a priori.
	 * @return List of input symbols
	 */
	static List<JimpleLocal> fakeFindInputSymbols(UnitGraph graph, int numArgs) {
		var units = graph.getBody().getUnits().stream().toList();
		return IntStream.range(0, numArgs).mapToObj(i ->
			((JimpleLocal) ((AbstractDefinitionStmt) units.get(i)).getLeftOp())
		).toList();
	}
}