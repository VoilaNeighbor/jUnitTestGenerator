package com.seideun.java.test.generator.new_constraint_solver;

import com.seideun.java.test.generator.constriant_solver.SootAgent;
import soot.toolkits.graph.UnitGraph;

class CompositeTypeConstraintSolvingTest extends ConstraintSolverTestBase {
	// 1 branch, 2 args.
	static final UnitGraph arrayExample =
		SootAgent.basicExamples.makeGraph("array");
}