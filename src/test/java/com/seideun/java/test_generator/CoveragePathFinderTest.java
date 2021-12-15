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
		SootMethod methodUnderAnalysis =
			classUnderTest.getMethodByName("sequential");
		UnitGraph controlFlowGraph =
			new ExceptionalUnitGraph(methodUnderAnalysis.retrieveActiveBody());

		List<List<Unit>> result = findCoveragePaths(controlFlowGraph);

		assertEquals(1, result.size());
		assertTrue(elementsEqual(
			controlFlowGraph.getBody().getUnits(),
			result.get(0)
		));
	}

	@Test
	void branchingMethodHasAPathForEachBranch() {
		SootMethod methodUnderAnalysis =
			classUnderTest.getMethodByName("twoBranches");
		UnitGraph controlFlowGraph =
			new ExceptionalUnitGraph(methodUnderAnalysis.retrieveActiveBody());
		// Todo(Seideun): if units is empty?

		List<List<Unit>> allPaths = findCoveragePaths(controlFlowGraph);

		List<Unit> units = new ArrayList<>(controlFlowGraph.getBody().getUnits());
		Set<List<Unit>> expected = new HashSet<>();
		expected.add(Arrays.asList(
			units.get(0), units.get(1), units.get(2), units.get(3), units.get(5)));
		expected.add(Arrays.asList(
			units.get(0), units.get(1), units.get(4), units.get(5)));

		assertEquals(expected.size(), allPaths.size());
		for (List<Unit> path: allPaths) {
			assertTrue(expected.contains(path));
		}
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
