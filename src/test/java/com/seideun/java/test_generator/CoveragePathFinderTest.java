package com.seideun.java.test_generator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The definition of <em>coverage path</em> can be different under circumstances.
 * For now, we are using the Prime-path method.
 *
 * <p> We expect all methods under test to return values, and we expect that
 * they all terminate, i.e. have no infinite loop.
 */
class CoveragePathFinderTest {
	static SootClass classUnderTest;

	// - Jump back to node before entrance on the current path.
	// - Jump back to node on other path, no matter visited or not.

	/**
	 * @param original domain of discourse
	 * @param indices  indices of elements to extract
	 * @return subset (still a list) of the original list.
	 */
	public static List<Unit> subsetOf(List<Unit> original, int... indices) {
		return IntStream.of(indices)
			.mapToObj(original::get)
			.collect(Collectors.toList());
	}

	private UnitGraph makeControlFlowGraph(String methodName) {
		SootMethod method = classUnderTest.getMethodByName(methodName);
		return new ExceptionalUnitGraph(method.retrieveActiveBody());
	}

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
		controlFlowGraph = makeControlFlowGraph("sequential");

		findCoveragePaths();

		assertEquals(1, result.size());
		assertTrue(elementsEqual(
			controlFlowGraph.getBody().getUnits(),
			result.get(0)
		));
	}

	@Test
	void branchingMethodHasAPathForEachBranch() {
		controlFlowGraph = makeControlFlowGraph("twoBranches");

		findCoveragePaths();

		Set<List<Unit>> expected = new HashSet<>();
		List<Unit> units = new ArrayList<>(controlFlowGraph.getBody().getUnits());
		expected.add(subsetOf(units, 0, 1, 2, 3, 5));
		expected.add(subsetOf(units, 0, 1, 4, 5));

		assertEquals(expected.size(), result.size());
		assertTrue(expected.containsAll(result));
	}

	@Test
	void jumpBackToLoopEntranceMakeLoopPaths() {
		controlFlowGraph = makeControlFlowGraph("jumpBackToLoopEntrance");
		findCoveragePaths();

		Set<List<Unit>> expected = new HashSet<>();
		List<Unit> units = new ArrayList<>(controlFlowGraph.getBody().getUnits());
	}

	public void findCoveragePaths() {
		result = new ArrayList<>();
		List<Unit> heads = controlFlowGraph.getHeads();
		assert heads.size() == 1 : "methods have only 1 entry point, I suppose?";
		findCoveragePaths(heads.get(0), new ArrayList<>());
	}

	/**
	 * @param thisUnit This unit under discourse. It is not in
	 *                 <code>before</code>.
	 *                 But it may be added to it just at the start of this method.
	 * @param thisPath takes ownership.
	 */
	private void findCoveragePaths(Unit thisUnit, List<Unit> thisPath) {
		thisPath.add(thisUnit);
		List<Unit> successors = controlFlowGraph.getSuccsOf(thisUnit);
		if (successors.isEmpty()) {
			result.add(thisPath);
		} else {
			for (Unit successor: successors) {
				findCoveragePaths(successor, new ArrayList<>(thisPath));
			}
		}
	}

	private UnitGraph controlFlowGraph;
	private List<List<Unit>> result;
}
