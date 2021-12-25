package com.seideun.java.test.generator.constriant_solver;

import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;
import soot.Unit;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JimpleLocal;

import java.util.ArrayList;
import java.util.List;

public class NewConstraintSolver {
	// A conflict between z3 and beautiful code is that z3 requires you to call
	// mkXXX and remember the symbols. This forces you to introduce cache
	// collections.
	private Context z3Context = new Context();

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
		// Sometimes, the symbol solver is unable to soundly solve a set of
		// constraints, nor can it prove it unsatisfiable. In this case, we
		// assign an arbitrary value to the mysterious symbols, instead of
		// giving up the whole case.
		//
		// We check if there are some unsolved symbols by examining the
		// returned model to see if the symbol is still an Expr, not a concrete
		// value.
		var result = new ArrayList<Object>();
		for (var i: inputSymbols) {
			result.add(0);
		}
		return result;
	}
}
