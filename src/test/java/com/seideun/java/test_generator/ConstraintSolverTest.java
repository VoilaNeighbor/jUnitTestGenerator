package com.seideun.java.test_generator;

import com.microsoft.z3.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintSolverTest {
	private final Context z3Context = new Context();
	private final Solver z3Solver = z3Context.mkSolver();

	@Test
	void solveSimpleIntConstraint() {
		IntExpr x = z3Context.mkIntConst("x");
		BoolExpr[] ast = z3Context.parseSMTLIB2String(
			"(declare-const x Int)" +
				"(assert (> x 26))" +
				"(assert (< x 30))" +
				"(check-sat)(get-model)",
			null, null, null, null
		);
		assertEquals(Status.SATISFIABLE, z3Solver.check(ast));
		Model model = z3Solver.getModel();
		int x_value = Integer.parseInt(model.eval(x, false).toString());
		assertTrue(x_value > 26);
		assertTrue(x_value < 30);
	}
}
