package com.seideun.java.test.generator.new_constriant_solver;

import com.seideun.java.test.generator.new_constraint_solver.ConstraintSolver;
import soot.toolkits.graph.UnitGraph;

import static com.seideun.java.test.generator.constriant_solver.SootAgent.exampleCfg;

class CompositeConstraintSolvingTest {
	// 1 branch, 2 args.
	static final UnitGraph branchExample = exampleCfg("twoBranches");
	ConstraintSolver solver = new ConstraintSolver();
}
