package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.seideun.java.test.generator.constriant_solver.TodoException;
import soot.IntType;
import soot.Local;
import soot.RefType;
import soot.Unit;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

/**
 * This class runs Jimple method body with Z3 expressions as
 * values. That is, it outputs a symbol table for each
 * path as the execution result, instead of concrete values.
 *
 * <h2>Terminology</h2>
 * <ul>
 *   <li>mkXx: make xxx.</li>
 *   <li>jXx: Jimple-related concepts.</li>
 *   <li>xxSymbol: Z3 Symbols, interchangeably called symbolic values</li>
 *   <li>XYMap: A table from X to Y.</li>
 * </ul>
 */
@SuppressWarnings("rawtypes")
public class JimpleSymbolicMachine {
	// public for test now.
	public final Context z3 = new Context();

	/**
	 * @return Symbol table of all paths run.
	 */
	public List<Map<JimpleLocal, Expr>> run(UnitGraph jProgram) {
		// By marking all symbols at the start, we save quite a lot of
		// online-checking burdens.
		var allJVars = markAllJVars(jProgram);
		return walkGraph(jProgram, allJVars);
	}

	private Map<JimpleLocal, Expr> markAllJVars(UnitGraph jProgram) {
		var result = new HashMap<JimpleLocal, Expr>();
		for (var jVar: jProgram.getBody().getLocals()) {
			result.put((JimpleLocal) jVar, switch (jVar.getType()) {
				case IntType x -> z3.mkIntConst(jVar.getName());
				case RefType x -> mkRefConst(jVar, x);
				default -> throw new TodoException(jVar);
			});
		}
		return result;
	}

	private Expr mkRefConst(Local jVar, RefType x) {
		var className = x.getClassName();
		if (className.equals(String.class.getName())) {
			return z3.mkConst(jVar.getName(), z3.mkStringSort());
		} else {
			throw new TodoException(x);
		}
	}

	private static List<Map<JimpleLocal, Expr>> walkGraph(UnitGraph jProgram, Map<JimpleLocal, Expr> allJVars) {
		var allPaths = new ArrayList<Map<JimpleLocal, Expr>>();
		for (Unit head: jProgram.getHeads()) {
			walkPath(head, new HashMap<>(allJVars), jProgram, allPaths);
		}
		return allPaths;
	}

	private static void walkPath(
		Unit thisUnit,
		Map<JimpleLocal, Expr> symbolTable,
		UnitGraph jProgram,
		List<Map<JimpleLocal, Expr>> completedPaths
	) {
		var successors = jProgram.getSuccsOf(thisUnit);
		if (successors.isEmpty()) {
			completedPaths.add(symbolTable);
			return;
		}
		for (Unit successor: successors) {
			walkPath(successor, new HashMap<>(symbolTable), jProgram, completedPaths);
		}
	}
}
