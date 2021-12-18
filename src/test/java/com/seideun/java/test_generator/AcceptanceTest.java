package com.seideun.java.test_generator;

import org.junit.jupiter.api.Test;
import soot.toolkits.graph.UnitGraph;

import java.util.List;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class AcceptanceTest {
	@Test
	void sequential() {
		SootAgent sootAgent = new SootAgent(ExampleCfgCases.class);
		UnitGraph controlFlowGraph = sootAgent.makeControlFlowGraph("sequential");
		ConstraintSolver constraintSolver = new ConstraintSolver();
		findPrimePaths(controlFlowGraph).forEach(constraintSolver::storePath);

		List<Object> synthesizedArgs = constraintSolver.synthesizeArguments().get();

		assertEquals(1, synthesizedArgs.size());
		assertTrue(synthesizedArgs.get(0) instanceof Integer);
	}
}
