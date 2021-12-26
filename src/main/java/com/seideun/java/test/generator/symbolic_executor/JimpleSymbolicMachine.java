package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is capable of running Jimple method body with Z3 expressions as
 * values (instead of concrete values). It outputs a Z3 Context and a symbol
 * table (jVar -> Z3-expr) as the result.
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

	public JsmState state() {
		return new JsmState(z3, symbolTable);
	}

	public void run(UnitGraph jProgram) {

	}
}
