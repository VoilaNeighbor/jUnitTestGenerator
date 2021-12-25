package com.seideun.java.test.generator.constriant_solver;

import org.junit.jupiter.api.Test;
import soot.jimple.internal.AbstractDefinitionStmt;

import java.util.ArrayList;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;
import static com.seideun.java.test.generator.constriant_solver.SootAgent.exampleCfg;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NewConstraintSolverTest {
	NewConstraintSolver solver = new NewConstraintSolver();

	@Test
	void findAllInputSymbols() {
		var exampleGraph = exampleCfg("twoArgs");
		var thePath = new ArrayList<>(findPrimePaths(exampleGraph).get(0));
		var inputSymbols = new ArrayList<>();
		var units = exampleGraph.getBody().getUnits().stream().toList();
		inputSymbols.add(((AbstractDefinitionStmt) units.get(0)).getLeftOp());
		inputSymbols.add(((AbstractDefinitionStmt) units.get(1)).getLeftOp());

		var result = solver.findAllInputSymbols(thePath);

		assertEquals(inputSymbols, result);
	}
}