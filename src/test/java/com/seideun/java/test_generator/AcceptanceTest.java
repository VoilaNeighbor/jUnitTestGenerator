package com.seideun.java.test_generator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import soot.toolkits.graph.UnitGraph;

import java.util.List;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class AcceptanceTest {
	SootAgent sootAgent = new SootAgent(ExampleCfgCases.class);

	@Test
	void sequential() {
		UnitGraph controlFlowGraph = sootAgent.makeControlFlowGraph("sequential");
		PathArgumentsSynthesizer solver = new PathArgumentsSynthesizer();
		solver.storePath(findPrimePaths(controlFlowGraph).get(0));

		List<Object> synthesizedArgs = solver.synthesizeArguments().get();

		assertEquals(1, synthesizedArgs.size());
		assertTrue(synthesizedArgs.get(0) instanceof Integer);
	}

	@Test
	@Disabled
	void twoBranches() {
		UnitGraph controlFlowGraph = sootAgent.makeControlFlowGraph("twoBranches");
		PathArgumentsSynthesizer finder = new PathArgumentsSynthesizer();

		List<Object> results = finder.synthesizeArguments().get();

		boolean branch1 = false;
		boolean branch2 = false;
		for (Object x: results) {
			if ((int) x < 1) {
				branch1 = true;
			} else {
				branch2 = true;
			}
		}

		assertTrue(branch1);
		assertTrue(branch2);
	}
}
