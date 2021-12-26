package com.seideun.java.test.generator.new_constraint_solver;

import com.microsoft.z3.Context;
import com.microsoft.z3.*;
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
 *
 * <h2>Terminology</h2>
 *
 * <ul>
 *   <li>jXx: Jimple-related concepts.</li>
 *   <li>xxSymbol: Z3 Symbols</li>
 *   <li>XYMap: A table from X to Y.</li>
 * </ul>
 */
@SuppressWarnings("unchecked")
public class JimpleAwareZ3Context extends Context {
	private final Map<JimpleLocal, Expr> symbolMap = new HashMap<>();
	private final Map<JimpleLocal, Expr> jArrToLenSymbolMap = new HashMap<>();

	public Expr getSymbol(Value jVar) {
		return symbolMap.get(((JimpleLocal) jVar));
	}

	/**
	 * JimpleLocal -> Current symbol in Z3.
	 */
	public Map<JimpleLocal, Expr> symbolMap() {
		return symbolMap;
	}

	/**
	 * JimpleLocal -> Current len symbol in Z3
	 */
	public Map<JimpleLocal, Expr> lengthMap() {
		return jArrToLenSymbolMap;
	}

	public Expr[] add(List<Switchable> jConstraints) {
		var result = new ArrayList<Expr>();
		for (var i: jConstraints) {
			result.add(add(i));
		}
		return result.toArray(new Expr[0]);
	}

	/**
	 * Add both the jVariables and constraints.
	 *
	 * @return added symbol. True if none.
	 * <p>
	 * Fixme: it mingles symbols and constraints!
	 */
	public Expr add(Switchable jimpleValue) {
		if (jimpleValue instanceof JInvertCondition x) {
			return mkNot(add(x.base));
		}
		return switch (jimpleValue) {
			case JNegExpr x -> mkMul(add(x.getOp()), mkInt(-1));
			case JGeExpr x -> mkGe(add(x.getOp1()), add(x.getOp2()));
			case JLeExpr x -> mkLe(add(x.getOp1()), add(x.getOp2()));
			case JGtExpr x -> mkGt(add(x.getOp1()), add(x.getOp2()));
			case JLtExpr x -> mkLt(add(x.getOp1()), add(x.getOp2()));
			case JEqExpr x -> mkEq(add(x.getOp1()), add(x.getOp2()));
			case JNeExpr x -> mkNot(mkEq(add(x.getOp1()), add(x.getOp2())));
			case JAndExpr x -> mkAnd(add(x.getOp1()), add(x.getOp2()));
			case JOrExpr x -> mkOr(add(x.getOp1()), add(x.getOp2()));
			case JXorExpr x -> mkXor(add(x.getOp1()), add(x.getOp2()));
			case JAddExpr x -> mkAdd(add(x.getOp1()), add(x.getOp2()));
			case JSubExpr x -> mkSub(add(x.getOp1()), add(x.getOp2()));
			case JMulExpr x -> mkMul(add(x.getOp1()), add(x.getOp2()));
			case JDivExpr x -> mkDiv(add(x.getOp1()), add(x.getOp2()));
			case JRemExpr x -> mkRem(add(x.getOp1()), add(x.getOp2()));
			case IntConstant x -> mkInt(x.value);
			case DoubleConstant x -> mkReal(x.value);
			case FloatConstant x -> mkReal(x.value);
			case JimpleLocal x -> getOrMakeSymbol(x);
			case JAssignStmt x -> extractConstraint(x);
			case JArrayRef x -> mkBoundaryCheck(x);
			// JLengthExpr can only appear in a JAssignStmt.
			default -> throw new TodoException(jimpleValue);
		};
	}

	private BoolExpr mkBoundaryCheck(JArrayRef x) {
		var jArray = ((JimpleLocal) x.getBase());
		int idx = ((IntConstant) x.getIndex()).value;
		var lenSymbol = jArrToLenSymbolMap.get(jArray);
		return mkLt(mkInt(idx), lenSymbol);
	}

	private BoolExpr extractConstraint(JAssignStmt x) {
		if (x.getRightOp() instanceof JLengthExpr y) {
			// The constraint of array lengths is not maintained in Z3.
			// Instead, we keep a map from JArrays to Length Symbols.
			jArrToLenSymbolMap.put(((JimpleLocal) y.getOp()), add(x.getLeftOp()));
			return mkTrue();
		} else {
			return mkEq(add(x.getLeftOp()), add(x.getRightOp()));
		}
	}

	private Expr<Sort> getOrMakeSymbol(JimpleLocal x) {
		var result = symbolMap.get(x);
		return result == null ? mkSymbol(x) : result;
	}

	private Expr mkSymbol(JimpleLocal x) {
		var result = mkConst(x.getName(), toSort(x.getType()));
		symbolMap.put(x, result);
		return result;
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
