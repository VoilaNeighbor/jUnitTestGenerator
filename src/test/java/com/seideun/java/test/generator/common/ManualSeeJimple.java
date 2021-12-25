package com.seideun.java.test.generator.common;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.seideun.java.test.generator.constriant_solver.SootAgent.exampleCfg;

@Disabled("Manual")
class ManualSeeJimple {
	@Test
	void see( ){
		var ug = exampleCfg("boolNegate");
	}
}
