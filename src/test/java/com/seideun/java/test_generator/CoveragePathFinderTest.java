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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.Iterables.elementsEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The definition of <em>coverage path</em> can be different under circumstances.
 * For now, we are using the Prime-path method.
 */
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

		List<List<Unit>> result = findCoveragePaths(controlFlowGraph);

		assertEquals(1, result.size());
		assertTrue(elementsEqual(
			controlFlowGraph.getBody().getUnits(),
			result.get(0)
		));
	}

	@Test
	void branchingMethodHasAPathForEachBranch() {
		UnitGraph controlFlowGraph = makeControlFlowGraph("twoBranches");
		// Todo(Seideun): if units is empty?

		List<List<Unit>> allPaths = findCoveragePaths(controlFlowGraph);

		Set<List<Unit>> expected = new HashSet<>();
		List<Unit> allUnits = new ArrayList<>(controlFlowGraph.getBody().getUnits());
		expected.add(subsetOf(allUnits, 0, 1, 2, 3, 5));
		expected.add(subsetOf(allUnits, 0, 1, 4, 5));

		assertEquals(expected.size(), allPaths.size());
		assertTrue(expected.containsAll(allPaths));
	}

	static List<Unit> subsetOf(List<Unit> original, int... indices) {
		return IntStream.of(indices)
			.mapToObj(original::get)
			.collect(Collectors.toList());
	}

	private UnitGraph makeControlFlowGraph(String methodName) {
		SootMethod method = classUnderTest.getMethodByName(methodName);
		return new ExceptionalUnitGraph(method.retrieveActiveBody());
	}

	public static List<List<Unit>> findCoveragePaths(UnitGraph controlFlowGraph) {
		List<List<Unit>> allPaths = new ArrayList<>();
		List<Unit> heads = controlFlowGraph.getHeads();
		assert heads.size() == 1 : "methods have only 1 entry point, I suppose?";
		for (Unit head: heads) {
			findCoveragePaths(head, new ArrayList<>(), controlFlowGraph, allPaths);
		}
		return allPaths;
	}

	/**
	 * @param thisUnit This unit under discourse. It is not in
	 *                 <code>before</code>.
	 *                 But it may be added to it just at the start of this method.
	 * @param thisPath takes ownership.
	 * @param graph    context of our search.
	 * @param result   container of all paths found.
	 */
	private static void findCoveragePaths(
		Unit thisUnit,
		List<Unit> thisPath,
		UnitGraph graph,
		List<List<Unit>> result
	) {
		thisPath.add(thisUnit);
		List<Unit> successors = graph.getSuccsOf(thisUnit);
		if (successors.isEmpty()) {
			result.add(thisPath);
		} else {
			successors.stream().skip(1).forEach(successor ->
				findCoveragePaths(successor, new ArrayList<>(thisPath), graph, result));
			findCoveragePaths(successors.get(0), thisPath, graph, result);
		}
	}
}
