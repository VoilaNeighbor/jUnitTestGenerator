package com.seideun.java.test_generator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import soot.*;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.List;

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
		UnitPatchingChain units = controlFlowGraph.getBody().getUnits();

		List<List<Unit>> result = new ArrayList<>();
		result.add(new ArrayList<>(units));

		assertEquals(1, result.size());
		assertTrue(elementsEqual(units, result.get(0)));
	}

	@Test
	void branchingTreeStructuredMethodHasAPathForEach() {
		SootMethod methodUnderAnalysis =
			classUnderTest.getMethodByName("twoBranches");
		UnitGraph controlFlowGraph =
			new ExceptionalUnitGraph(methodUnderAnalysis.retrieveActiveBody());
		UnitPatchingChain units = controlFlowGraph.getBody().getUnits();
	}
}
