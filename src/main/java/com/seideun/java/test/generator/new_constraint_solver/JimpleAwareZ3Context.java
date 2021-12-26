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
 *   <li>mkXx: make xxx.</li>
 *   <li>jXx: Jimple-related concepts.</li>
 *   <li>xxSymbol: Z3 Symbols, interchangeably called symbolic values</li>
 *   <li>XYMap: A table from X to Y.</li>
 * </ul>
 */
@SuppressWarnings("unchecked")
public class JimpleAwareZ3Context extends Context {
	/**
	 * The same jVar can map to different Symbolic values
	 */
	private final Map<JimpleLocal, Expr> jVarToSymbolMap = new HashMap<>();
	private final Map<JimpleLocal, Expr> jArrToLenSymbolMap = new HashMap<>();

	public Map<JimpleLocal, Expr> symbolMap() {
		return jVarToSymbolMap;
	}

	public Map<JimpleLocal, Expr> lengthMap() {
		return jArrToLenSymbolMap;
	}

	/**
	 * Effectively AND the individual constraints.
	 *
	 * @return the AND-ed constraints.
	 */
	public Expr addConstraints(List<Switchable> jConstraints) {
		var result = new ArrayList<Expr>();
		for (var i: jConstraints) {
			result.add(addConstraint(i));
		}
		return mkAnd(result.toArray(new Expr[0]));
	}

	/**
	 * Converts JConstraints to Z3 constraints.
	 * If a JVar is never met before, it will be added too by the way.
	 * If a JAssignStmt is found, the symbol will be updated, too.
	 * <p>
	 * The returned constraint might not be boolean. E.g. int arithmetics may
	 * result in IntExpr.
	 *
	 * @return added constraints. True if none. Note that the added symbols are
	 * not returned!
	 */
	public Expr addConstraint(Switchable jConstraint) {
		return switch (jConstraint) {
			case JInvertCondition x -> mkNot(addConstraint(x.base));
			case AbstractBinopExpr x -> addBinaryExpr(x);
			default -> addSpecialConstraints(jConstraint);
		};
	}

	/**
	 * Maps both jConstants and jVariables to Z3 symbols.
	 */
	public Expr toSymbol(Value jVar) {
		return switch (jVar) {
			case IntConstant x -> mkInt(x.value);
			case DoubleConstant x -> mkReal(x.value);
			case FloatConstant x -> mkReal(x.value);
			default -> getOrMkSymbol((JimpleLocal) jVar);
		};
	}

	private Expr addSpecialConstraints(Switchable jConstraint) {
		return switch (jConstraint) {
			case JNegExpr x -> mkMul(toSymbol(x.getOp()), mkInt(-1));
			case JAssignStmt x -> handleAssignment(x);
			case JArrayRef x -> mkBoundaryCheck(x);
			// JLengthExpr can only appear in a JAssignStmt.
			default -> throw new TodoException(jConstraint);
		};
	}

	private Expr addBinaryExpr(AbstractBinopExpr abe) {
		var lhs = abe.getOp1();
		var rhs = abe.getOp2();
		return switch (abe) {
			case JGeExpr x -> mkGe(toSymbol(lhs), toSymbol(rhs));
			case JLeExpr x -> mkLe(toSymbol(lhs), toSymbol(rhs));
			case JGtExpr x -> mkGt(toSymbol(lhs), toSymbol(rhs));
			case JLtExpr x -> mkLt(toSymbol(lhs), toSymbol(rhs));
			case JEqExpr x -> mkEq(toSymbol(lhs), toSymbol(rhs));
			case JNeExpr x -> mkNot(mkEq(toSymbol(lhs), toSymbol(rhs)));
			case JAndExpr x -> mkAnd(toSymbol(lhs), toSymbol(rhs));
			case JOrExpr x -> mkOr(toSymbol(lhs), toSymbol(rhs));
			case JXorExpr x -> mkXor(toSymbol(lhs), toSymbol(rhs));
			case JAddExpr x -> mkAdd(toSymbol(lhs), toSymbol(rhs));
			case JSubExpr x -> mkSub(toSymbol(lhs), toSymbol(rhs));
			case JMulExpr x -> mkMul(toSymbol(lhs), toSymbol(rhs));
			case JDivExpr x -> mkDiv(toSymbol(lhs), toSymbol(rhs));
			case JRemExpr x -> mkRem(toSymbol(lhs), toSymbol(rhs));
			default -> throw new TodoException(abe);
		};
	}

	/**
	 * Updates jVar -> Symbols mapping, and returns constraints in implied in the
	 * assignment.
	 */
	private BoolExpr handleAssignment(JAssignStmt x) {
		var lhs = x.getLeftOp();
		var rhs = x.getRightOp();
		if (lhs instanceof JArrayRef y) {
			var jArr = (JimpleLocal) y.getBase();
			var oldArrSymbol = jVarToSymbolMap.get(jArr);
			var index = toSymbol(y.getIndex());
			var newValue = toSymbol(rhs);
			// Todo(Seideun): Can an array assigning stmt have complex expressions
			//                on the right? In that case we need addConstraint.
			//                But maybe we need a unifying method.
			var newArrSymbol = mkStore(oldArrSymbol, index, newValue);
			jVarToSymbolMap.put(jArr, newArrSymbol);
			return mkBoundaryCheck(y);
		} else if (rhs instanceof JLengthExpr y) {
			var jArr = (JimpleLocal) y.getOp();
			var jLen = (JimpleLocal) lhs;
			jVarToSymbolMap.put(jArr, getOrMkSymbol(jArr));
			jVarToSymbolMap.put(jLen, getOrMkSymbol(jLen));
			jArrToLenSymbolMap.put(jArr, getOrMkSymbol(jLen));
			return mkTrue();
		} else {
			// In the normal case, the RHS is an expression, and the LHS is a jVar.
			return mkEq(toSymbol(lhs), addConstraint(rhs));
		}
	}

	private BoolExpr mkBoundaryCheck(JArrayRef jArrayRef) {
		var jArray = ((JimpleLocal) jArrayRef.getBase());
		int idx = ((IntConstant) jArrayRef.getIndex()).value;
		var lenSymbol = jArrToLenSymbolMap.get(jArray);
		return mkLt(mkInt(idx), lenSymbol);
	}

	private Expr<Sort> getOrMkSymbol(JimpleLocal x) {
		var result = jVarToSymbolMap.get(x);
		return result == null ? mkSymbol(x) : result;
	}

	private Expr mkSymbol(JimpleLocal x) {
		var result = mkConst(x.getName(), toSort(x.getType()));
		jVarToSymbolMap.put(x, result);
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
