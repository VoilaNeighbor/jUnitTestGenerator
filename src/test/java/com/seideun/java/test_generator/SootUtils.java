package com.seideun.java.test_generator;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class SootUtils {
	private static final Scene sootScene;

	static {
		final String rootClasspath =
			System.getProperty("user.dir") + "/target/test-classes";
		Options sootConfigs = Options.v();
		sootConfigs.set_prepend_classpath(true);
		sootConfigs.set_soot_classpath(rootClasspath);
		sootScene = Scene.v();
	}

	public static SootClass loadClass(String className) {
		SootClass result = sootScene.loadClassAndSupport(className);
		// Manually-loaded SootClass's are not set as app class by default.
		// I don't know of another way to load them yet. So let's bundle the setup
		// code as here.
		result.setApplicationClass();
		sootScene.loadNecessaryClasses();
		return result;
	}

	public static UnitGraph makeControlFlowGraph(String methodName, SootClass theClass) {
		SootMethod method = theClass.getMethodByName(methodName);
		return new ExceptionalUnitGraph(method.retrieveActiveBody());
	}
}
