package com.seideun.java.test_generator;

import com.microsoft.z3.*;
import org.junit.jupiter.api.Test;

import static java.lang.Double.parseDouble;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintSolverTest {
	private final Context z3Context = new Context();
	private final Solver z3Solver = z3Context.mkSolver();

	@Test
	void solveSimpleIntConstraint() {
		IntExpr x = z3Context.mkIntConst("x");
		RealExpr y = z3Context.mkRealConst("y");

		BoolExpr[] ast = z3Context.parseSMTLIB2String(
			"(declare-const x Int)" +
				"(declare-const y Real)" +
				"(assert (> x 26))" +
				"(assert (< x 30))" +
				"(assert (> y 3.338))" +
				"(assert (< y 3.352))" +
				"(check-sat)(get-model)",
			null, null, null, null
		);
		assertEquals(Status.SATISFIABLE, z3Solver.check(ast));
		Model model = z3Solver.getModel();

		int x_value = Integer.parseInt(model.eval(x, false).toString());
		assertTrue(x_value > 26);
		assertTrue(x_value < 30);

		String[] y_str = model.eval(y, false).toString().split("/");
		double y_value = parseDouble(y_str[0]) / parseDouble(y_str[1]);
		assertTrue(y_value > 3.338);
		assertTrue(y_value < 3.352);
	}
}
