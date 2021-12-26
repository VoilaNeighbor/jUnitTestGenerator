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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hi! I'm a special Z3 Context which also knows Jimple well!
 */
@SuppressWarnings("unchecked")
public class JimpleAwareZ3Context extends Context {
	// JimpleLocal -> Current symbolic value
	private final Map<JimpleLocal, Expr> valueMap = new HashMap<>();

	public Expr[] add(List<Switchable> jConstraints) {
		var result = new ArrayList<Expr>();
		for (var i: jConstraints) {
			var expr = addSimple(i);
			if (expr == null) {
				addComplex(i, result);
			} else {
				result.add(expr);
			}
		}
		return result.toArray(new Expr[0]);
	}

	public Expr addSimple(Switchable jimpleValue) {
		if (jimpleValue instanceof JInvertCondition x) {
			return mkNot(addSimple(x.base));
		}
		return switch (jimpleValue) {
			case JGeExpr x -> mkGe(addSimple(x.getOp1()), addSimple(x.getOp2()));
			case JLeExpr x -> mkLe(addSimple(x.getOp1()), addSimple(x.getOp2()));
			case JGtExpr x -> mkGt(addSimple(x.getOp1()), addSimple(x.getOp2()));
			case JLtExpr x -> mkLt(addSimple(x.getOp1()), addSimple(x.getOp2()));
			case JEqExpr x -> mkEq(addSimple(x.getOp1()), addSimple(x.getOp2()));
			case JNeExpr x -> mkNot(mkEq(addSimple(x.getOp1()), addSimple(x.getOp2())));
			case JAddExpr x -> mkAdd(addSimple(x.getOp1()), addSimple(x.getOp2()));
			case JSubExpr x -> mkSub(addSimple(x.getOp1()), addSimple(x.getOp2()));
			case JMulExpr x -> mkMul(addSimple(x.getOp1()), addSimple(x.getOp2()));
			case JDivExpr x -> mkDiv(addSimple(x.getOp1()), addSimple(x.getOp2()));
			case JRemExpr x -> mkRem(addSimple(x.getOp1()), addSimple(x.getOp2()));
			case JAssignStmt x -> mkEq(addSimple(x.getLeftOp()), addSimple(x.getRightOp()));
			case JimpleLocal x -> mkConst(x.getName(), toSort(x.getType()));
			case IntConstant x -> mkInt(x.value);
			case DoubleConstant x -> mkReal(x.value);
			case FloatConstant x -> mkReal(x.value);
			default -> null;
		};
	}

	public void addComplex(Switchable jConstraint, List<Expr> out) {
		switch (jConstraint) {
		case JimpleLocal x -> {
			var y = valueMap.get(x);
			if (y != null) {
				out.add(y);
			} else {
				var symbolicValue = mkConst(x.getName(), toSort(x.getType()));
				valueMap.put(x, symbolicValue);
				// Todo(Seideun): Rename. This is required in path for loops.
			}
		}
		default -> throw new TodoException(jConstraint);
		}
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
