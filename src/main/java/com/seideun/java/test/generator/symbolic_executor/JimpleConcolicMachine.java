package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.Context;
import com.microsoft.z3.*;
import com.seideun.java.test.generator.constriant_solver.Rational;
import soot.*;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.NumericConstant;
import soot.jimple.internal.*;
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
	private final Context z3 = new Context();
	private final Solver solver = z3.mkSolver();
	// Total map: jVars -> current-symbolic-values. Their symbolic values can be
	// updated as the execution goes.
	private final Map<JimpleLocal, Expr> symbolTable = new HashMap<>();
	// The current jimple program we are analyzing. Cached as a field for
	// referencing. Soot does a nice job in that it stores out-of-the-box
	// information for us to check.
	private UnitGraph jProgram;
	// Remember what we were to solve.
	private Map<JimpleLocal, Expr> inputSymbolTable;

	/**
	 * @return Concrete values, one table for each possible path.
	 */
	public List<Map<JimpleLocal, Object>> run(UnitGraph jProgram) {
		this.jProgram = jProgram;
		rememberInputJVars();
		loadAllJVars();
		return runAllPaths();
	}

	// By marking all symbols at the start, we save quite a lot of
	// online-checking burdens.
	private void loadAllJVars() {
		symbolTable.clear();
		for (var jVar: jProgram.getBody().getLocals()) {
			symbolTable.put((JimpleLocal) jVar, switch (jVar.getType()) {
				case IntType x -> z3.mkIntConst(jVar.getName());
				case RefType x -> mkRefConst(jVar, x);
				default -> todo(jVar);
			});
		}
	}

	private void rememberInputJVars() {
		var inputSymbols = new HashMap<JimpleLocal, Expr>();
		for (var jVar: jProgram.getBody().getParameterLocals()) {
			inputSymbols.put((JimpleLocal) jVar, switch (jVar.getType()) {
				case IntType x -> z3.mkIntConst(jVar.getName());
				case RefType x -> mkRefConst(jVar, x);
				default -> todo(jVar);
			});
		}
		inputSymbolTable = Collections.unmodifiableMap(inputSymbols);
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
	private List<Map<JimpleLocal, Object>> runAllPaths() {
		var allConcreteValues = new ArrayList<Map<JimpleLocal, Object>>();
		for (Unit head: jProgram.getHeads()) {
			solver.push();
			runJStmt(head, allConcreteValues);
			solver.pop();
		}
		return allConcreteValues;
	}

	private void runJStmt(Unit thisUnit, List<Map<JimpleLocal, Object>> result) {
		if (thisUnit == null) {
			return;
		}
		switch (thisUnit) {
		case JAssignStmt assignStmt -> {
			var lhs = assignStmt.getLeftOp();
			var rhs = assignStmt.getRightOp();
			var oldSymbol = symbolTable.put((JimpleLocal) lhs, map(rhs));
			final var successors = jProgram.getSuccsOf(thisUnit);
			assert successors.size() <= 1;
			if (successors.isEmpty()) {
				result.add(solveCurrentConstraints());
			} else {
				runJStmt(successors.get(0), result);
			}
			symbolTable.put((JimpleLocal) lhs, oldSymbol);
		}
		case JIfStmt ifStmt -> {
			var constraint = map(ifStmt.getCondition());
			assert jProgram.getSuccsOf(ifStmt).size() == 2;
			for (Unit next: jProgram.getSuccsOf(ifStmt)) {
				solver.push();
				if (next == ifStmt.getTarget()) {
					solver.add(constraint);
				} else {
					solver.add(z3.mkNot(constraint));
				}
				runJStmt(next, result);
				solver.pop();
			}
		}
		default -> {
			final var successors = jProgram.getSuccsOf(thisUnit);
			assert successors.size() <= 1;
			if (successors.isEmpty()) {
				result.add(solveCurrentConstraints());
			} else {
				runJStmt(successors.get(0), result);
			}
		}
		}
	}

	/**
	 * Maps jValue to Z3 expression. There are 3 cases:
	 * 1. The jValue is a constant. In this case, we make a trivial constant.
	 * 2. The jValue is a local. Since we have recorded all locals in our symbol
	 * table, we simply look it up for the corresponding Z3 Symbol(expression).
	 * 3. The jValue is a composite expression. In this case, we translate it
	 * recursively into a z3 expression tree.
	 * Todo(Seideun): Maybe a better name?
	 */
	private Expr map(Value jValue) {
		return switch (jValue) {
			case NumericConstant c -> switch (c) {
				case IntConstant x -> z3.mkInt(x.value);
				case DoubleConstant x -> mkReal(x.value);
				case FloatConstant x -> mkReal(x.value);
				default -> todo(jValue);
			};
			case JimpleLocal jVar -> symbolTable.get(jVar);
			case AbstractBinopExpr abe -> {
				var lhs = abe.getOp1();
				var rhs = abe.getOp2();
				yield switch (abe) {
					case JGeExpr x -> z3.mkGe(map(lhs), map(rhs));
					case JLeExpr x -> z3.mkLe(map(lhs), map(rhs));
					case JGtExpr x -> z3.mkGt(map(lhs), map(rhs));
					case JLtExpr x -> z3.mkLt(map(lhs), map(rhs));
					case JEqExpr x -> z3.mkEq(map(lhs), map(rhs));
					case JNeExpr x -> z3.mkNot(z3.mkEq(map(lhs), map(rhs)));
					case JAndExpr x -> z3.mkAnd(map(lhs), map(rhs));
					case JOrExpr x -> z3.mkOr(map(lhs), map(rhs));
					case JXorExpr x -> z3.mkXor(map(lhs), map(rhs));
					case JAddExpr x -> z3.mkAdd(map(lhs), map(rhs));
					case JSubExpr x -> z3.mkSub(map(lhs), map(rhs));
					case JMulExpr x -> z3.mkMul(map(lhs), map(rhs));
					case JDivExpr x -> z3.mkDiv(map(lhs), map(rhs));
					case JRemExpr x -> z3.mkRem(map(lhs), map(rhs));
					default -> todo(abe);
				};
			}
			default -> todo(jValue);
		};
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

		var result = new HashMap<JimpleLocal, Object>();
		for (var kv: inputSymbolTable.entrySet()) {
			var jArgument = kv.getKey();
			var symbolicValue = kv.getValue();
			var interpretation = model.eval(symbolicValue, true);
			if (interpretation instanceof IntNum x) {
				result.put(jArgument, x.getInt());
			} else if (interpretation instanceof SeqExpr<?> x) {
				result.put(jArgument, x.getString());
			} else {
				todo(interpretation);
			}
		}
		return result;
	}

	/* ****************** Utilities ****************** */

	private RealExpr mkReal(double x) {
		Rational rational = new Rational(x);
		return z3.mkReal(rational.numerator, rational.denominator);
	}

	private static <T> T todo(Object ignored) {
		System.err.printf("<Todo>Ignoring unknown object: %s</Todo>\n", ignored);
		return null;
	}
}
