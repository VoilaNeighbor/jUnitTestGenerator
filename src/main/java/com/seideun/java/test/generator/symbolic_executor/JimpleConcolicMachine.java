package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.*;
import com.seideun.java.test.generator.constriant_solver.TodoException;
import soot.IntType;
import soot.Local;
import soot.RefType;
import soot.Unit;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class JimpleConcolicMachine {
	private final Context z3 = new Context();
	private final Solver solver = z3.mkSolver();
	// Total map: jVars -> current-symbolic-values. Their symbolic values can be
	// updated as the execution goes.
	private final Map<JimpleLocal, Expr> symbolTable = new HashMap<>();
	// The current jimple program we are analyzing. Cached as a field for
	// referencing. Soot does a nice job in that it stores out-of-the-box
	// information for us to check.
	private UnitGraph jProgram;

	/**
	 * @return Concrete values, one table for each possible path.
	 */
	public List<Map<JimpleLocal, Object>> run(UnitGraph jProgram) {
		this.jProgram = jProgram;
		constructSymbolTable();
		return walkGraph();
	}

	// By marking all symbols at the start, we save quite a lot of
	// online-checking burdens.
	private void constructSymbolTable() {
		symbolTable.clear();
		for (var jVar: jProgram.getBody().getLocals()) {
			symbolTable.put((JimpleLocal) jVar, switch (jVar.getType()) {
				case IntType x -> z3.mkIntConst(jVar.getName());
				case RefType x -> mkRefConst(jVar, x);
				default -> todo(jVar);
			});
		}
	}

	// Maybe refactor this into a new class?
	private Expr mkRefConst(Local jVar, RefType x) {
		var className = x.getClassName();
		if (className.equals(String.class.getName())) {
			return z3.mkConst(jVar.getName(), z3.mkStringSort());
		} else {
			return todo(x);
		}
	}

	/**
	 * Walk all rationally-feasible paths, and construct a concrete value table
	 * for each path solvable.
	 *
	 * @return A list of sets of arguments, each corresponding to a unique path.
	 */
	private List<Map<JimpleLocal, Object>> walkGraph() {
		var allConcreteValues = new ArrayList<Map<JimpleLocal, Object>>();
		for (Unit head: jProgram.getHeads()) {
			solver.push();
			walkPath(head, allConcreteValues);
			solver.pop();
		}
		return allConcreteValues;
	}

	private void walkPath(Unit thisUnit, List<Map<JimpleLocal, Object>> result) {
		var successors = jProgram.getSuccsOf(thisUnit);
		if (successors.isEmpty()) {
			result.add(solveCurrentConstraints());
		} else if (successors.size() == 1) {
			walkPath(successors.get(0), result);
		} else {
			for (Unit successor: successors) {
				solver.push();
				walkPath(successor, result);
				solver.pop();
			}
		}
	}

	/**
	 * Find concrete values of parameter jVars by interpreting the corresponding
	 * symbols.
	 */
	private Map<JimpleLocal, Object> solveCurrentConstraints() {
		var status = solver.check();
		if (status != Status.SATISFIABLE) {
			return todo(status);
		}
		var model = solver.getModel();

		var parameters = jProgram.getBody().getParameterLocals();
		var concreteValues = new HashMap<JimpleLocal, Object>();
		for (Local i: parameters) {
			var parameter = (JimpleLocal) i;
			var symbolicValue = symbolTable.get(parameter);
			var interpretation = model.eval(symbolicValue, true);
			if (interpretation instanceof IntNum x) {
				concreteValues.put(parameter, x.getInt());
			} else if (interpretation instanceof SeqExpr<?> x) {
				concreteValues.put(parameter, x.getString());
			} else {
				throw new TodoException(interpretation);
			}
		}

		return concreteValues;
	}

	private static <T> T todo(Object ignored) {
		System.err.printf("<Todo>Ignoring unknown object: %s</Todo>\n", ignored);
		return null;
	}
}
