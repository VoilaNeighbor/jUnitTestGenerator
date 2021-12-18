package com.seideun.java.test_generator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
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
		solver.store(findPrimePaths(controlFlowGraph).get(0));

		List<Object> synthesizedArgs = solver.synthesizeArguments().get();

		assertEquals(1, synthesizedArgs.size());
		assertTrue(synthesizedArgs.get(0) instanceof Integer);
	}

	@Test
	void twoBranches() {
		UnitGraph controlFlowGraph = sootAgent.makeControlFlowGraph("twoBranches");

		boolean branch1 = false;
		boolean branch2 = false;

		List<List<Object>> result =
			PathArgumentsSynthesizer.synthesize(findPrimePaths(controlFlowGraph));
		assertEquals(2, result.size());

		for (List<Object> args: result) {
			assertEquals(1, args.size());
			if ((int) args.get(0) < 1) {
				branch1 = true;
			} else {
				branch2 = true;
			}
		}

		assertTrue(branch1);
		assertTrue(branch2);
	}
}
