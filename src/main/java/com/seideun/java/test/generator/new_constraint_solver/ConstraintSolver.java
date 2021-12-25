package com.seideun.java.test.generator.new_constraint_solver;

import com.microsoft.z3.*;
import com.seideun.java.test.generator.constriant_solver.Rational;
import com.seideun.java.test.generator.constriant_solver.TodoException;
import org.apache.commons.lang3.tuple.Pair;
import soot.Unit;
import soot.Value;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.internal.*;

import java.util.ArrayList;
import java.util.List;

// A conflict between z3 and beautiful code is that z3 requires you to call
// mkXXX and remember the symbols. This forces you to introduce cache
// collections.
public class ConstraintSolver extends Context {
	/**
	 * @param path Starting from the method entrance. Is a list to enforce
	 *             orderliness.
	 * @return Input symbols in order of encounter.
	 */
	public List<JimpleLocal> findAllInputSymbols(List<Unit> path) {
		// Input locals are defined by JIdentityStmt in Soot.
		return path.stream().sequential()
			.filter(x -> x instanceof JIdentityStmt)
			.map(x -> ((JimpleLocal) ((JIdentityStmt) x).getLeftOp()))
			.toList();
	}

	public List<Object> solveSymbols(
		List<JimpleLocal> inputSymbols,
		List<Unit> path
	) {
		// Todo(Seideun): Finish here
		// Sometimes, the symbol solver is unable to soundly solve a set of
		// constraints, nor can it prove it unsatisfiable. In this case, we
		// assign an arbitrary value to the mysterious symbols, instead of
		// giving up the whole case.
		//
		// We check if there are some unsolved symbols by examining the
		// returned model to see if the symbol is still an Expr, not a concrete
		// value.
		var result = new ArrayList<>();
		for (var i: inputSymbols) {
			result.add(0);
		}
		return result;
	}

	public Pair<Object, Status> solveOneConstraint(
		JimpleLocal inputSymbol,
		AbstractBinopExpr constraint
	) {
		var z3Symbol = mkIntConst(inputSymbol.getName());
		var solver = mkSolver();
		var status = solver.check(makeZ3Expr(constraint));
		if (status != Status.SATISFIABLE) {
			return Pair.of(null, status);
		}
		var model = solver.getModel();
		return Pair.of(evaluateToObject(z3Symbol, model), status);
	}

	private Expr makeZ3Expr(Value jimpleValue) {
		return switch (jimpleValue) {
			case JGeExpr x -> mkGe(makeZ3Expr(x.getOp1()), makeZ3Expr(x.getOp2()));
			case JLeExpr x -> mkLe(makeZ3Expr(x.getOp1()), makeZ3Expr(x.getOp2()));
			case JGtExpr x -> mkGt(makeZ3Expr(x.getOp1()), makeZ3Expr(x.getOp2()));
			case JLtExpr x -> mkLt(makeZ3Expr(x.getOp1()), makeZ3Expr(x.getOp2()));
			case JEqExpr x -> mkEq(makeZ3Expr(x.getOp1()), makeZ3Expr(x.getOp2()));
			// should fail: Can we make the same symbol multiple times?
			case JimpleLocal x -> mkIntConst(x.getName());
			case IntConstant x -> mkInt(x.value);
			case DoubleConstant x -> mkReal(x.value);
			case FloatConstant x -> mkReal(x.value);
			default -> throw new TodoException(jimpleValue);
		};
	}

	private Object evaluateToObject(Expr z3Symbol, Model model) {
		// More on the 2nd bool parameter in `model.eval`:
		// https://z3prover.github.io/api/html/group__capi.html#gadb6ff55c26f5ef5607774514ee184957
		// Simply put, if the parameter is true, unbounded symbols will be assigned
		// some arbitrary values too. This simplifies our logic here.
		return switch (model.eval(z3Symbol, true)) {
			case IntNum x -> x.getInt();
			default -> throw new TodoException(z3Symbol);
		};
	}

	private RealExpr mkReal(double x) {
		Rational rational = new Rational(x);
		return mkReal(rational.numerator, rational.denominator);
	}
}
