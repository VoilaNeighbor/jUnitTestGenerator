package com.seideun.java.test.generator.CFG_generator;

import soot.*;
import soot.options.Options;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;

//负责生成控制流图相关信息
public class SootCFG {
	public static UnitGraph getMethodCFG(
		String sourceDirectory,
		String clsName,
		String methodName
	) {
		G.reset();
		Options.v().set_prepend_classpath(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_soot_classpath(sourceDirectory);
		SootClass sc = Scene.v().loadClassAndSupport(clsName);
		// They are the ones which can be freely analysed & modified.
		sc.setApplicationClass();
		Scene.v().loadNecessaryClasses();
		// This is the same on as `sc`. Damn the coder is without a proper head.
		SootClass mainClass = Scene.v().getSootClass(clsName);
		SootMethod sm = mainClass.getMethodByName(methodName);
		Body body = sm.retrieveActiveBody();
		return new ClassicCompleteUnitGraph(body);
	}
}
