package com.seideun.java.test.generator.constriant_solver;

import soot.Unit;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JimpleLocal;

import java.util.Collection;
import java.util.List;

public class NewConstraintSolver {
	/**
	 * @param path Starting from the method entrance.
	 * @return Input symbols in order of encounter.
	 */
	public List<JimpleLocal> findAllInputSymbols(Collection<Unit> path) {
		// Input locals are defined by JIdentityStmt in Soot.
		return path.stream()
			.filter(x -> x instanceof JIdentityStmt)
			.map(x -> ((JimpleLocal) ((JIdentityStmt) x).getLeftOp()))
			.toList();
	}
}
