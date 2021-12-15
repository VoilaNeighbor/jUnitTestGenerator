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
		double result = solveReal("y", new String[]{ "> y 588.821" });
		assertTrue(result > 588.821);
	}

	@Test
	void solveSingleConstraintsOnSinglePositiveInt() {
		int result = solveInt("x", new String[]{ "> x 26" });
		assertTrue(result > 26);
	}

//	@Test
//	void solveMultipleConstraints() {
//	}

	// Think: Do we need to extract using template method now?
	private double solveReal(String variable, String[] constraint) {
		String smtLibLang = assembleSmtLibLang(variable, constraint, "Real");
		Model resultModel = makeResultModel(smtLibLang);

		RealExpr vExpr = z3Context.mkRealConst(variable);
		String[] rational = resultModel.eval(vExpr, false).toString().split("/");
		String numerator = rational[0];
		String denominator = rational[1];

		double result = Double.parseDouble(numerator) / Double.parseDouble(
			denominator);
		return result;
	}

	private int solveInt(String variable, String[] constraint) {
		String smtLibLang = assembleSmtLibLang(variable, constraint, "Int");
		Model resultModel = makeResultModel(smtLibLang);

		IntExpr vExpr = z3Context.mkIntConst(variable);
		return Integer.parseInt(resultModel.eval(vExpr, false).toString());
	}

	private Model makeResultModel(String smtLibLang) {
		BoolExpr[] ast = z3Context.parseSMTLIB2String(
			smtLibLang, null, null, null, null
		);
		z3Solver.check(ast);

		return z3Solver.getModel();
	}

	private String assembleSmtLibLang(
		String variable,
		String[] constraint,
		String type
	) {
		StringBuilder builder = new StringBuilder();
		builder.append("(declare-const ")
			.append(variable)
			.append(" ")
			.append(type)
			.append(")");
		for (String c: constraint) {
			builder
				.append("(assert (")
				.append(c)
				.append("))");
		}
		return builder.toString();
	}
}
