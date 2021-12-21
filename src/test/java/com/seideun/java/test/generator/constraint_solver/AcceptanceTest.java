package com.seideun.java.test.generator.constraint_solver;

import com.seideun.java.test.generator.CFG_analyzer.Path;
import com.seideun.java.test.generator.constriant_solver.PathArgumentsSynthesizer;
import org.junit.jupiter.api.Test;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

import java.util.List;
import java.util.stream.Collectors;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;
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

	@Test
	void jumpBackToLoopEntrance() {
		UnitGraph controlFlowGraph =
			sootAgent.makeControlFlowGraph("jumpBackToLoopEntrance");
		List<List<Unit>> completePaths = findPrimePaths(controlFlowGraph).stream()
			.map(p -> new Path(controlFlowGraph, p).oneCompletePath)
			.collect(Collectors.toList());

		boolean branch1 = false;
		boolean branch2 = false;

		List<List<Object>> result =
			PathArgumentsSynthesizer.synthesize(completePaths);

		for (List<Object> args: result) {
			assertEquals(1, args.size());
			if ((int) args.get(0) < 20) {
				branch1 = true;
			} else {
				branch2 = true;
			}
		}

		assertTrue(branch1);
		assertTrue(branch2);
	}
}
