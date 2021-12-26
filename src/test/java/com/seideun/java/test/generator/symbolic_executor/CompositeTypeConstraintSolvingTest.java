package com.seideun.java.test.generator.symbolic_executor;

import com.seideun.java.test.generator.constriant_solver.SootAgent;
import soot.toolkits.graph.UnitGraph;

class CompositeTypeConstraintSolvingTest extends ConstraintSolverTestBase {
	// 1 branch, 2 args.
	static final UnitGraph arrayExample =
		SootAgent.basicExamples.makeGraph("array");
}
