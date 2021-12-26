package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.seideun.java.test.generator.constriant_solver.TodoException;
import soot.IntType;
import soot.RefType;
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
public class JimpleSymbolicMachine {
	private final Context z3 = new Context();
	private final Map<JimpleLocal, Expr> symbolTable = new HashMap<>();

	/**
	 * @return A symbol table for each path executed.
	 */
	public List<Map<JimpleLocal, Expr>> resultPaths() {
		return Collections.singletonList(symbolTable);
	}

	public void run(UnitGraph jProgram) {
		var body = jProgram.getBody();
		for (var jVar: body.getLocals()) {
			addSymbol((JimpleLocal) jVar);
		}
	}

	private void addSymbol(JimpleLocal jVar) {
		symbolTable.put(jVar, switch (jVar.getType()) {
			case IntType x -> z3.mkIntConst(jVar.getName());
			case RefType x -> mkRefConst(jVar, x);
			default -> throw new TodoException(jVar);
		});
	}

	private Expr mkRefConst(JimpleLocal jVar, RefType x) {
		var className = x.getClassName();
		if (className.equals(String.class.getName())) {
			return z3.mkConst(jVar.getName(), z3.mkStringSort());
		} else {
			throw new TodoException(x);
		}
	}
}
