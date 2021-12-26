package com.seideun.java.test.generator.new_constriant_solver;

import com.microsoft.z3.Status;
import com.seideun.java.test.generator.constriant_solver.SootAgent;
import org.junit.jupiter.api.Test;
import soot.IntType;
import soot.jimple.IntConstant;
import soot.jimple.internal.*;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;
import static org.junit.jupiter.api.Assertions.*;

class BasicConstraintSolvingTest extends ConstraintSolverTestBase {
	// No constraints, 2 arguments.
	static final UnitGraph trivialExample =
		SootAgent.basicExamples.makeGraph(
		"twoArgs");

	@Test
	void findInputSymbols() {
		var thePath = new ArrayList<>(findPrimePaths(trivialExample).get(0));

		var result = solver.findInputSymbols(thePath);

		assertEquals(fakeFindInputSymbols(trivialExample, 2), result);
	}

	@Test
	void findConstraints() {
		// I'm being lazy here. Logic included:
		// 1. JIfStmt extract conditions, and negate if do not jump.
		// 2. JAssignStmt reserved.
		var ug = SootAgent.basicExamples.makeGraph("twoBranches");
		var paths = findPrimePaths(ug);
		var constraints = paths.stream().map(solver::findConstraints).toList();
		assertEquals(
			"[[i0 >= 1, $b1 = 9], [i0 !( >= ) 1, $b1 = 6]]",
			constraints.toString()
		);
	}

	@Test
	void solveOneConstraint() {
		var symbol = new JimpleLocal("x", IntType.v());
		var conceivedConstraint = new JGeExpr(symbol, IntConstant.v(1));

		var result = solver.findConcreteValueOf(symbol, conceivedConstraint);

		assertEquals(Status.SATISFIABLE, result.getRight());
		assertTrue((int) result.getLeft() >= 1);
	}

	@Test
	void solveTwoConstraints() {
		var symbol = new JimpleLocal("x", IntType.v());
		var constraint1 = new JGeExpr(symbol, IntConstant.v(-10));
		var constraint2 = new JLtExpr(symbol, IntConstant.v(-5));

		var result = solver.findConcreteValueOf(
			symbol,
			List.of(constraint1, constraint2)
		);

		assertEquals(Status.SATISFIABLE, result.getRight());
		assertTrue((int) result.getLeft() >= -10);
		assertTrue((int) result.getLeft() < -5);
	}

	@Test
	void reportUnsatisfiableConstraints() {
		var symbol = new JimpleLocal("x", IntType.v());
		var conceivedConstraint = new JGtExpr(symbol, symbol);

		var result = solver.findConcreteValueOf(symbol, conceivedConstraint);

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