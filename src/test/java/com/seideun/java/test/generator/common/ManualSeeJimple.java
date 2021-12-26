package com.seideun.java.test.generator.common;

import com.seideun.java.test.generator.constriant_solver.SootAgent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Manual")
class ManualSeeJimple {
	@Test
	void see( ){
		var ug = SootAgent.basicExamples.makeGraph("boolNegate");
	}
}
