package com.seideun.java.test.generator.new_constraint_solver;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Sort;
import com.seideun.java.test.generator.constriant_solver.Rational;
import com.seideun.java.test.generator.constriant_solver.TodoException;
import soot.*;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.internal.*;
import soot.util.Switchable;

import java.util.List;

/**
 * Hi! I'm a special Z3 Context which also knows Jimple well!
 */
@SuppressWarnings("unchecked")
public class JimpleAwareZ3Context extends Context {
	public Expr[] add(List<Switchable> jimpleValues) {
		return jimpleValues.stream()
			.map(this::add)
			.toList()
			.toArray(new Expr[]{});
	}

	public Expr add(Switchable jimpleValue) {
		if (jimpleValue instanceof JInvertCondition x) {
			return add(x.base);
		}
		return switch (jimpleValue) {
			case JGeExpr x -> mkGe(add(x.getOp1()), add(x.getOp2()));
			case JLeExpr x -> mkLe(add(x.getOp1()), add(x.getOp2()));
			case JGtExpr x -> mkGt(add(x.getOp1()), add(x.getOp2()));
			case JLtExpr x -> mkLt(add(x.getOp1()), add(x.getOp2()));
			case JEqExpr x -> mkEq(add(x.getOp1()), add(x.getOp2()));
			case JNeExpr x -> mkNot(mkEq(add(x.getOp1()), add(x.getOp2())));
			case JAddExpr x -> mkAdd(add(x.getOp1()), add(x.getOp2()));
			case JSubExpr x -> mkSub(add(x.getOp1()), add(x.getOp2()));
			case JMulExpr x -> mkMul(add(x.getOp1()), add(x.getOp2()));
			case JDivExpr x -> mkDiv(add(x.getOp1()), add(x.getOp2()));
			case JRemExpr x -> mkRem(add(x.getOp1()), add(x.getOp2()));
			case JAssignStmt x -> mkEq(add(x.getLeftOp()), add(x.getRightOp()));
			case JimpleLocal x -> mkConst(x.getName(), toSort(x.getType()));
			case IntConstant x -> mkInt(x.value);
			case DoubleConstant x -> mkReal(x.value);
			case FloatConstant x -> mkReal(x.value);
			default -> throw new TodoException(jimpleValue);
		};
	}

	private Sort toSort(Type sootType) {
		return switch (sootType) {
			case IntType y -> mkIntSort();
			case DoubleType y -> mkRealSort();
			case FloatType y -> mkRealSort();
			case ArrayType y -> mkArraySort(mkIntSort(), toSort(y.baseType));
			default -> throw new TodoException(sootType);
		};
	}

	private RealExpr mkReal(double x) {
		Rational rational = new Rational(x);
		return mkReal(rational.numerator, rational.denominator);
	}
}
