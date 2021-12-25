package com.seideun.java.test.generator.new_constriant_solver;

import soot.toolkits.graph.UnitGraph;

import static com.seideun.java.test.generator.constriant_solver.SootAgent.exampleCfg;

class CompositeTypeConstraintSolvingTest extends ConstraintSolverTestBase {
	// 1 branch, 2 args.
	static final UnitGraph branchExample = exampleCfg("twoBranches");
}
