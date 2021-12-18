package com.seideun.java.test_generator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.Iterables.elementsEqual;
import static com.seideun.java.test_generator.CoveragePathFinder.findCoveragePaths;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoveragePathFinderTest {
	static SootClass classUnderTest;

	@BeforeAll
	static void setupSoot() {
		final String rootClasspath =
			System.getProperty("user.dir") + "/target/test-classes";
		Options sootConfigs = Options.v();
		sootConfigs.set_prepend_classpath(true);
		sootConfigs.set_soot_classpath(rootClasspath);

		// Manually-loaded SootClass's are not set as app class by default.
		// I don't know of another way to load them yet. So let's bundle the setup
		// code as here.
		Scene sootScene = Scene.v();
		final String classname = "com.seideun.java.test_generator.ExampleCfgCases";
		classUnderTest = sootScene.loadClassAndSupport(classname);
		classUnderTest.setApplicationClass();
		sootScene.loadNecessaryClasses();
	}

	/**
	 * Sequential methods, i.e. methods with no branches and only one source,
	 * have only one coverage path which is the whole method.
	 */
	@Test
	void sequentialMethodHasSolePath() {
		UnitGraph controlFlowGraph = makeControlFlowGraph("sequential");

		List<List<Unit>> coveragePaths = findCoveragePaths(controlFlowGraph);

		assertEquals(1, coveragePaths.size());
		assertTrue(elementsEqual(
			controlFlowGraph.getBody().getUnits(),
			coveragePaths.get(0)
		));
	}

	UnitGraph makeControlFlowGraph(String methodName) {
		SootMethod method = classUnderTest.getMethodByName(methodName);
		return new ExceptionalUnitGraph(method.retrieveActiveBody());
	}

	// - Jump back to node at entrance of loop.
	// - Jump back to node before entrance on the current path.
	// - Jump back to node on other path, no matter visited or not.

	@Test
	void branchingMethodHasAPathForEachBranch() {
		UnitGraph controlFlowGraph = makeControlFlowGraph("twoBranches");

		List<List<Unit>> coveragePaths = findCoveragePaths(controlFlowGraph);

		Set<List<Unit>> expected = new HashSet<>();
		List<Unit> units = new ArrayList<>(controlFlowGraph.getBody().getUnits());
		expected.add(subsetOf(units, 0, 1, 2, 3, 5));
		expected.add(subsetOf(units, 0, 1, 4, 5));

		assertEquals(expected.size(), coveragePaths.size());
		assertTrue(expected.containsAll(coveragePaths));
	}

	/**
	 * @param original domain of discourse
	 * @param indices  indices of elements to extract
	 * @return subset (still a list) of the original list.
	 */
	static List<Unit> subsetOf(List<Unit> original, int... indices) {
		return IntStream.of(indices)
			.mapToObj(original::get)
			.collect(Collectors.toList());
	}
}
