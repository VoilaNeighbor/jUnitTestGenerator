package com.seideun.java.test.generator.new_constriant_solver;

import org.junit.jupiter.api.Test;
import soot.toolkits.graph.UnitGraph;

import static com.seideun.java.test.generator.constriant_solver.SootAgent.exampleCfg;
import static com.seideun.java.test.generator.constriant_solver.SootAgent.exampleCompositeTypes;

class CompositeTypeConstraintSolvingTest extends ConstraintSolverTestBase {
	// 1 branch, 2 args.
	static final UnitGraph arrayExample = exampleCompositeTypes("array");

	@Test
	void arrayTypes() {
		System.out.println(arrayExample);
	}
}
