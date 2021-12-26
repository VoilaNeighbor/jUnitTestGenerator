package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.Status;
import org.apache.commons.lang3.tuple.Pair;
import soot.Unit;
import soot.jimple.internal.JimpleLocal;
import soot.util.Switchable;

import java.util.List;

public class ConstraintSolverTestBase {
	JimpleSolver solver = new JimpleSolver();

	void clear() {
		solver = new JimpleSolver();
	}

	void reportStates(
		List<Unit> path,
		List<JimpleLocal> inputs,
		List<Switchable> constraints,
		Pair<List<Object>, Status> result
	) {
		System.out.printf("<path>%s<path>\n", path);
		System.out.printf("<inputSymbols>%s<inputSymbols>\n", inputs);
		System.out.printf("<constraints>%s<constraints>\n", constraints);
		System.out.printf("<result>%s<result>\n\n", result);
	}
}
