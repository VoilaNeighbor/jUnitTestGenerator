package com.seideun.java.test.generator.constriant_solver;

import com.seideun.java.test.generator.examples.BasicExamples;
import com.seideun.java.test.generator.examples.CompositeTypeExamples;
import com.seideun.java.test.generator.examples.JsmExamples;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import static java.lang.System.getProperty;

/**
 * I'm an agent that configures and talks with Soot!
 */
public class SootAgent {
	public static final SootAgent jsmExamples;
	public static final SootAgent basicExamples;
	public static final SootAgent compositeExamples;
	private static final Scene sootScene;
	private final SootClass sootClass;

	static {
		final var rootClasspath = getProperty("user.dir") + "/target/classes";
		var sootConfigs = Options.v();
		sootConfigs.set_prepend_classpath(true);
		sootConfigs.set_soot_classpath(rootClasspath);
		sootScene = Scene.v();
		jsmExamples = new SootAgent(JsmExamples.class);
		basicExamples = new SootAgent(BasicExamples.class);
		compositeExamples = new SootAgent(CompositeTypeExamples.class);
		sootScene.loadNecessaryClasses();
	}

	private SootAgent(Class<?> theClass) {
		sootClass = sootScene.loadClassAndSupport(theClass.getName());
		sootClass.setApplicationClass();
	}

	public UnitGraph makeGraph(String methodName) {
		SootMethod method = sootClass.getMethodByName(methodName);
		return new ExceptionalUnitGraph(method.retrieveActiveBody());
	}
}
