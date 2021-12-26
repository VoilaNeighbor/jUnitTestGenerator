package com.seideun.java.test.generator.new_constriant_solver;

import com.seideun.java.test.generator.constriant_solver.SootAgent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import soot.Unit;

import java.util.List;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;

@Disabled
class ManualSolverTest extends ConstraintSolverTestBase {
	@Test
	@Disabled
	void solveCompleteCase() {
		var ug = SootAgent.basicExamples.makeGraph("equalComparison");
		var paths = findPrimePaths(ug);
		for (List<Unit> path: paths) {
			var inputs = solver.findInputSymbols(path);
			var constraints = solver.findConstraints(path);
			var result = solver.findConcreteValueOf(inputs, constraints);
			reportStates(path, inputs, constraints, result);
		}
	}
}
