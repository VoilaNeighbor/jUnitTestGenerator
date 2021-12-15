package com.seideun.java.test_generator;

import com.microsoft.z3.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * - solve double types
 * - solve negative numbers.
 */
class ConstraintSolverTest {
	private final Context z3Context = new Context();
	private final Solver z3Solver = z3Context.mkSolver();

	@Test
	void solveSingleConstraintOnSinglePositiveDouble() {
		String variable = "y";
		String constraint = "> y 588.821";

		String smtLibLang
			= "(declare-const " + variable + " Real)(assert (" + constraint + "))";

		BoolExpr[] ast = z3Context.parseSMTLIB2String(
			smtLibLang, null, null, null, null
		);
		z3Solver.check(ast);

		Model resultModel = z3Solver.getModel();
		RealExpr vExpr = z3Context.mkRealConst(variable);
		String[] rational = resultModel.eval(vExpr, false).toString().split("/");
		String numerator = rational[0];
		String denominator = rational[1];

		double result = Double.parseDouble(numerator) / Double.parseDouble(denominator);

		assertTrue(result > 588.821);
	}

	@Test
	void solveSingleConstraintsOnSinglePositiveInt() {
		int result = solveInt("x", "> x 26");
		assertTrue(result > 26);
	}

	private int solveInt(String variable, String constraint) {
		String smtLibLang
			= "(declare-const " + variable + " Int)(assert (" + constraint + "))";

		BoolExpr[] ast = z3Context.parseSMTLIB2String(
			smtLibLang, null, null, null, null
		);
		z3Solver.check(ast);

		Model resultModel = z3Solver.getModel();
		IntExpr vExpr = z3Context.mkIntConst(variable);
		return Integer.parseInt(resultModel.eval(vExpr, false).toString());
	}
}
