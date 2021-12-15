package com.seideun.java.test_generator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

class CodeGraphGeneratorTest {
	static final String CLASSPATH =
		System.getProperty("user.dir") + "/target/test-classes";
	static SootClass class_under_test;

	@BeforeAll
	static void setupSoot() {
		Options sootConfigs = Options.v();
		sootConfigs.set_prepend_classpath(true);
		sootConfigs.set_soot_classpath(CLASSPATH);
		Scene sootScene = Scene.v();
		class_under_test = sootScene.loadClassAndSupport("cut.LogicStructure");
	}

	@Test
	void canRun() {

	}
}
