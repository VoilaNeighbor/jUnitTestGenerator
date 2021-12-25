package com.seideun.java.test.generator.new_constraint_solver;

import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.RealExpr;
import com.seideun.java.test.generator.constriant_solver.Rational;
import com.seideun.java.test.generator.constriant_solver.TodoException;
import soot.Value;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.internal.*;

import java.util.List;

/**
 * Hi! I'm a special Z3 Context which also knows Jimple well!
 */
@SuppressWarnings("unchecked")
public class JimpleAwareZ3Context extends Context {
	public Expr[] add(List<Value> jimpleValues) {
		return jimpleValues.stream()
			.map(this::add)
			.toList()
			.toArray(new Expr[]{});
	}

	public Expr add(Value jimpleValue) {
		return switch (jimpleValue) {
			case JGeExpr x -> mkGe(add(x.getOp1()), add(x.getOp2()));
			case JLeExpr x -> mkLe(add(x.getOp1()), add(x.getOp2()));
			case JGtExpr x -> mkGt(add(x.getOp1()), add(x.getOp2()));
			case JLtExpr x -> mkLt(add(x.getOp1()), add(x.getOp2()));
			case JEqExpr x -> mkEq(add(x.getOp1()), add(x.getOp2()));
			// should fail: Can we make the same symbol multiple times?
			case JimpleLocal x -> mkIntConst(x.getName());
			case IntConstant x -> mkInt(x.value);
			case DoubleConstant x -> mkReal(x.value);
			case FloatConstant x -> mkReal(x.value);
			default -> throw new TodoException(jimpleValue);
		};
	}

	private RealExpr mkReal(double x) {
		Rational rational = new Rational(x);
		return mkReal(rational.numerator, rational.denominator);
	}
}
