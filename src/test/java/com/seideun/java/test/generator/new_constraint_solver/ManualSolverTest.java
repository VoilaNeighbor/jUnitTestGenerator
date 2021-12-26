package com.seideun.java.test.generator.new_constraint_solver;

import com.seideun.java.test.generator.constriant_solver.SootAgent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

import java.util.List;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;

@Disabled
class ManualSolverTest extends ConstraintSolverTestBase {
	UnitGraph basic = SootAgent.basicExamples.makeGraph("equalComparison");
	UnitGraph loop = SootAgent.basicExamples.makeGraph("loop");
	UnitGraph array = SootAgent.compositeExamples.makeGraph("array");
	UnitGraph store = SootAgent.compositeExamples.makeGraph("arrayStore");

	@Test
	void solveCompleteCase() {
		inspect(store);
	}

	private void inspect(UnitGraph graph) {
		var paths = findPrimePaths(graph);
		for (List<Unit> path: paths) {
			clear();
			var inputs = solver.findInputSymbols(path);
			var constraints = solver.findConstraints(path);
			var result = solver.findConcreteValueOf(inputs, constraints);
			reportStates(path, inputs, constraints, result);
		}
	}
}
