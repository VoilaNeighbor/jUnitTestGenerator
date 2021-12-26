package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.seideun.java.test.generator.constriant_solver.TodoException;
import soot.IntType;
import soot.Local;
import soot.RefType;
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
		var body = jProgram.getBody();
		for (var jVar: body.getLocals()) {
			addSymbol((JimpleLocal) jVar);
		}
	}

	private void addSymbol(JimpleLocal jVar) {
		symbolTable.put(jVar, switch (jVar.getType()) {
			case IntType x -> z3.mkIntConst(jVar.getName());
			case RefType x -> {
				var className = x.getClassName();
				if (className.equals(String.class.getName())) {
					yield z3.mkString(jVar.getName());
				} else {
					throw new TodoException(x);
				}
			}
			default -> throw new TodoException(jVar);
		});
	}
}
