package com.seideun.java.test_generator;

import com.microsoft.z3.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * It is too weird to parse to SMT-LIB strings! We could have used the Java API
 * directly. We were misled by the damn example our teacher gave us. Dang.
 */
@Deprecated
@Disabled
class OldStringBasedConstraintSolverTest {
	final Context z3Context = new Context();
	final Solver z3Solver = z3Context.mkSolver();

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

	@Test
	void solveMultipleConstraints() {
		double result = solveReal("x", new String[]{ "> x 1.118", "< x 1.119" });
		assertTrue(result > 1.118);
		assertTrue(result < 1.119);
	}

	@Test
	void solveWithNewApi() {
		IntExpr x = z3Context.mkIntConst("x");
		IntNum _26 = z3Context.mkInt(26);
		BoolExpr x_gt_26 = z3Context.mkGt(x, _26);
		assertEquals(Status.SATISFIABLE, z3Solver.check(x_gt_26));
		Model model = z3Solver.getModel();
		System.out.println(model);
	}

	// Think: Do we need to extract using template method now?
	public double solveReal(String variable, String[] constraints) {
		String smtLibLang = assembleSmtLibLang(variable, constraints, "Real");
		Model resultModel = makeResultModel(smtLibLang);

		RealExpr vExpr = z3Context.mkRealConst(variable);
		Expr<RealSort> interpretation = resultModel.eval(vExpr, false);
		String[] rational = interpretation.toString().split("/");
		String numerator = rational[0];
		String denominator = rational[1];

		return Double.parseDouble(numerator) / Double.parseDouble(denominator);
	}

	public int solveInt(String variable, String[] constraints) {
		String smtLibLang = assembleSmtLibLang(variable, constraints, "Int");
		Model resultModel = makeResultModel(smtLibLang);

		IntExpr vExpr = z3Context.mkIntConst(variable);
		Expr<IntSort> interpretation = resultModel.eval(vExpr, false);
		return Integer.parseInt(interpretation.toString());
	}

	private Model makeResultModel(String smtLibLang) {
		BoolExpr[] ast = z3Context.parseSMTLIB2String(
			smtLibLang, null, null, null, null
		);
		z3Solver.check(ast);

		return z3Solver.getModel();
	}

	enum Z3Type {
		INT, REAL;

		@Override
		public String toString() {
			if (this == INT) {
				return "Int";
			} else {
				return "Real";
			}
		}
	}

	class Z3Variable {
		public final String name;
		public final Z3Type z3Type;

		public Z3Variable(String name, Z3Type z3Type) {
			this.name = name;
			this.z3Type = z3Type;
		}
	}

	@Deprecated
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

	/**
	 * Assemble into the <a href="https://smtlib.cs.uiowa.edu/">SMT-LIB</a>
	 * standard language.
	 */
	private static String assembleSmtLibLang(
		Z3Variable[] variables,
		String[] constraints
	) {
		StringBuilder builder = new StringBuilder();
		for (Z3Variable v: variables) {
			builder.append("(declare-const ")
				.append(v.name)
				.append(" ")
				.append(v.z3Type)
				.append(")");
		}
		for (String c: constraints) {
			builder
				.append("(assert (")
				.append(c)
				.append("))");
		}
		return builder.toString();
	}
}
