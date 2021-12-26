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
 *   <li>xxSymbol: Z3 Symbols</li>
 *   <li>XYMap: A table from X to Y.</li>
 * </ul>
 */
@SuppressWarnings("unchecked")
public class JimpleAwareZ3Context extends Context {
	private final Map<JimpleLocal, Expr> symbolMap = new HashMap<>();
	private final Map<JimpleLocal, Expr> jArrToLenSymbolMap = new HashMap<>();

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

	public Expr[] addConstraints(List<Switchable> jConstraints) {
		var result = new ArrayList<Expr>();
		for (var i: jConstraints) {
			result.add(addConstraint(i));
		}
		return result.toArray(new Expr[0]);
	}

	/**
	 * Converts JConstraints to Z3 constraints.
	 * If a JVar is never met before, it will be added too by the way.
	 * If a JAssignStmt is found, the symbol will be updated, too.
	 *
	 * @return added constraints. True if none. Note that the added symbols are
	 * not returned!
	 */
	public Expr addConstraint(Switchable jConstraint) {
		return switch (jConstraint) {
			case JInvertCondition x -> mkNot(addConstraint(x.base));
			case AbstractBinopExpr x -> addBinaryConstraint(x);
			default -> addSpecialConstraints(jConstraint);
		};
	}

	private Expr addSpecialConstraints(Switchable jConstraint) {
		return switch (jConstraint) {
			case JNegExpr x -> mkMul(toSymbol(x.getOp()), mkInt(-1));
			case JAssignStmt x -> extractConstraint(x);
			case JArrayRef x -> mkBoundaryCheck(x);
			// JLengthExpr can only appear in a JAssignStmt.
			default -> throw new TodoException(jConstraint);
		};
	}

	private Expr addBinaryConstraint(AbstractBinopExpr abe) {
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

	private BoolExpr mkBoundaryCheck(JArrayRef x) {
		var jArray = ((JimpleLocal) x.getBase());
		int idx = ((IntConstant) x.getIndex()).value;
		var lenSymbol = jArrToLenSymbolMap.get(jArray);
		return mkLt(mkInt(idx), lenSymbol);
	}

	private BoolExpr extractConstraint(JAssignStmt x) {
		if (x.getRightOp() instanceof JLengthExpr y) {
			var jArr = ((JimpleLocal) y.getOp());
			var jLen = ((JimpleLocal) x.getLeftOp());
			jArrToLenSymbolMap.put(jArr, getOrMkSymbol(jLen));
			return mkTrue();
		} else {
			return mkEq(addConstraint(x.getLeftOp()), addConstraint(x.getRightOp()));
		}
	}

	private Expr<Sort> getOrMkSymbol(JimpleLocal x) {
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
