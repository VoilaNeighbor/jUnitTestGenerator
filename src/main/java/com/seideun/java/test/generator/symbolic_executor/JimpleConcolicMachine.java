package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.*;
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
public class JimpleConcolicMachine {
	// public for test now.
	private final Context z3 = new Context();
	private final Solver solver = z3.mkSolver();
	private UnitGraph jProgram;

	/**
	 * @return Concrete values, one table for each possible path.
	 */
	public List<Map<JimpleLocal, Object>> run(UnitGraph jProgram) {
		this.jProgram = jProgram;
		// By marking all symbols at the start, we save quite a lot of
		// online-checking burdens.
		var allJVars = markAllJVars();
		return walkGraph(allJVars);
	}

	private Map<JimpleLocal, Expr> markAllJVars() {
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

	private List<Map<JimpleLocal, Object>> walkGraph(Map<JimpleLocal, Expr> allJVars) {
		var allPaths = new ArrayList<Map<JimpleLocal, Expr>>();
		var allConcreteValues = new ArrayList<Map<JimpleLocal, Object>>();
		for (Unit head: jProgram.getHeads()) {
			solver.push();
			walkPath(head, new HashMap<>(allJVars), allPaths, allConcreteValues);
			solver.pop();
		}
		return allConcreteValues;
	}

	private void walkPath(
		Unit thisUnit,
		Map<JimpleLocal, Expr> symbolTable,
		// Let's replace this
		List<Map<JimpleLocal, Expr>> completedPaths,
		List<Map<JimpleLocal, Object>> allConcreteValues
		// By this: List<Map<JimpleLocal, Object>>
	) {
		var successors = jProgram.getSuccsOf(thisUnit);
		if (successors.isEmpty()) {
			allConcreteValues.add(solve(symbolTable));
			completedPaths.add(symbolTable);
		} else if (successors.size() == 1) {
			walkPath(successors.get(0), symbolTable, completedPaths, allConcreteValues);
		} else {
			for (Unit successor: successors) {
				walkPath(successor, new HashMap<>(symbolTable), completedPaths, allConcreteValues);
			}
		}
	}

	private Map<JimpleLocal, Object> solve(
		Map<JimpleLocal, Expr> symbolTable
	) {
		var status = solver.check();
		if (status != Status.SATISFIABLE) {
			throw new TodoException(status);
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
			} else if (interpretation instanceof SeqExpr<?> x){
				concreteValues.put(parameter, x.getString());
			} else {
				throw new TodoException(interpretation);
			}
		}

		return concreteValues;
	}
}
