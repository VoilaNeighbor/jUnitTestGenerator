package com.seideun.java.test.generator.constriant_solver;

import org.junit.jupiter.api.Test;
import soot.jimple.internal.AbstractDefinitionStmt;

import java.util.ArrayList;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;
import static com.seideun.java.test.generator.constriant_solver.SootAgent.exampleCfg;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NewConstraintSolverTest {
	NewConstraintSolver solver = new NewConstraintSolver();

	// Input locals are of type JParameterRef in Soot.
	@Test
	void findAllInputSymbols() {
		var exampleGraph = exampleCfg("twoArgs");
		var aSimplePath = new ArrayList<>(findPrimePaths(exampleGraph).get(0));
		var expected = new ArrayList<>() {{
			var units = exampleGraph.getBody().getUnits().stream().toList();
			add(((AbstractDefinitionStmt) units.get(0)).getLeftOp());
			add(((AbstractDefinitionStmt) units.get(1)).getLeftOp());
		}};

		var result = solver.findAllInputSymbols(aSimplePath);

		assertEquals(expected, result);
	}
}