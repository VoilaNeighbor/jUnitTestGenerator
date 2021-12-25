package com.seideun.java.test.generator.constriant_solver;

import com.seideun.java.test.generator.examples.ExampleCfgCases;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * I'm an agent that configures and talks with Soot!
 */
public class SootAgent {
	private static final Scene sootScene;

	static {
		final String rootClasspath =
			System.getProperty("user.dir") + "/target/classes";
		Options sootConfigs = Options.v();
		sootConfigs.set_prepend_classpath(true);
		sootConfigs.set_soot_classpath(rootClasspath);
		sootScene = Scene.v();
	}

	private final SootClass classUnderAnalyses;

	public SootAgent(Class<?> theClass) {
		classUnderAnalyses = sootScene.loadClassAndSupport(theClass.getName());
		classUnderAnalyses.setApplicationClass();
		sootScene.loadNecessaryClasses();
	}

	public UnitGraph makeControlFlowGraph(String methodName) {
		SootMethod method = classUnderAnalyses.getMethodByName(methodName);
		return new ExceptionalUnitGraph(method.retrieveActiveBody());
	}

	private static final SootAgent exampleCfgInstance =
		new SootAgent(ExampleCfgCases.class);
	public static UnitGraph exampleCfg(String methodName) {
		return exampleCfgInstance.makeControlFlowGraph(methodName);
	}
}
