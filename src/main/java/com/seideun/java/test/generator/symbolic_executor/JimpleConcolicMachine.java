package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.*;
import com.seideun.java.test.generator.constriant_solver.Rational;
import com.seideun.java.test.generator.constriant_solver.TodoException;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

import static java.util.stream.Stream.concat;

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
	private final Map<Local, Expr> symbolTable = new HashMap<>();
	// The current jimple program we are analyzing. Cached as a field for
	// referencing. Soot does a nice job in that it stores out-of-the-box
	// information for us to check.
	private UnitGraph jProgram;
	// Remember what we were to solve.
	private Map<Local, Expr> inputSymbolTable;
	// If a node that is being visited is found again, that means we have
	// found a loop.
	private Set<Unit> visiting = new HashSet<>();

	/**
	 * @return Concrete values, one table for each possible path.
	 */
	public List<Map<Local, Object>> run(UnitGraph jProgram) {
		this.jProgram = jProgram;
		rememberInputJVars();
		loadAllJVars();
		return run();
	}

	// By marking all symbols at the start, we save quite a lot of
	// online-checking burdens.
	private void loadAllJVars() {
		symbolTable.clear();
		buildMapping(symbolTable, jProgram.getBody().getLocals());
	}

	private void rememberInputJVars() {
		var inputSymbols = new HashMap<Local, Expr>();
		var jVars = jProgram.getBody().getParameterLocals();
		buildMapping(inputSymbols, jVars);
		inputSymbolTable = Collections.unmodifiableMap(inputSymbols);
	}

	private void buildMapping(Map<Local, Expr> mapping, Iterable<Local> jVars) {
		for (var jVar: jVars) {
			mapping.put(jVar, switch (jVar.getType()) {
				case IntType x -> z3.mkIntConst(jVar.getName());
				case ByteType x -> {
					var result = z3.mkIntConst(jVar.getName());
					solver.add(z3.mkLe(z3.mkInt(-128), result));
					solver.add(z3.mkLe(result, z3.mkInt(127)));
					yield result;
				}
				case BooleanType x -> z3.mkBoolConst(jVar.getName());
				case DoubleType x -> z3.mkRealConst(jVar.getName());
				case RefType x -> mkRefConst(jVar, x);
				case ArrayType x -> {
					// Make a array
					var baseSort = switch (x.baseType) {
						case IntType y -> z3.mkIntSort();
						default -> todo(x.baseType);
					};
					yield z3.mkConst(jVar.getName(), z3.mkSeqSort(((Sort) baseSort)));
				}
				default -> todo(jVar);
			});
		}
	}

	/**
	 * Walk all rationally-feasible paths, and construct a concrete value table
	 * for each path solvable.
	 *
	 * @return A list of sets of arguments, each corresponding to a unique path.
	 */
	private List<Map<Local, Object>> run() {
		var allConcreteValues = new ArrayList<Map<Local, Object>>();
		for (Unit head: jProgram.getHeads()) {
			solver.push();
			runJStmt(head, allConcreteValues);
			solver.pop();
		}
		return allConcreteValues;
	}

	private void runJStmt(Unit thisUnit, List<Map<Local, Object>> result) {
		if (visiting.contains(thisUnit)) {
			result.add(solveCurrentConstraints());
			return;
		}
		visiting.add(thisUnit);
		switch (thisUnit) {
		case JAssignStmt assignStmt -> {
			var lhs = assignStmt.getLeftOp();
			var rhs = assignStmt.getRightOp();
			if (lhs instanceof Local x) {
				var oldSymbol = symbolTable.put(x, map(rhs));
				runNext(thisUnit, result);
				symbolTable.put(x, oldSymbol);
			} else if (lhs instanceof ArrayRef x) {
				var jArr = (Local) x.getBase();
				var oldArraySymbol = map(jArr);
				var lenSymbol = z3.mkLength(oldArraySymbol);
				var ancestorArrSymbol = inputSymbolTable.get(jArr);
				var idxSymbol = map(x.getIndex()); // Also prefixLen
				solver.add(z3.mkGt(z3.mkLength(ancestorArrSymbol), idxSymbol));
				var prefixSeq = z3.mkExtract(oldArraySymbol, z3.mkInt(0), idxSymbol);
				var postfixSeq = z3.mkExtract(
					oldArraySymbol,
					z3.mkAdd(z3.mkInt(2), idxSymbol),
					z3.mkSub(lenSymbol, z3.mkAdd(z3.mkInt(1), idxSymbol))
				);
				var middle = z3.mkUnit(map(rhs));
				var newArraySymbol = z3.mkConcat(prefixSeq, middle, postfixSeq);
				var oldInTable = symbolTable.put(jArr, newArraySymbol);
				runNext(thisUnit, result);
				symbolTable.put(jArr, oldInTable);
			} else {
				todo(lhs);
			}
		}
		case JIfStmt ifStmt -> {
			assert jProgram.getSuccsOf(ifStmt).size() == 2;
			for (Unit next: jProgram.getSuccsOf(ifStmt)) {
				var constraint = next == ifStmt.getTarget()
					? map(ifStmt.getCondition())
					: z3.mkNot(map(ifStmt.getCondition()));
				solver.push();
				solver.add(constraint);
				runJStmt(next, result);
				solver.pop();
			}
		}
		// Statements that do not affect symbol values are ignored.
		default -> runNext(thisUnit, result);
		}
		visiting.remove(thisUnit);
	}

	private void runNext(Unit thisUnit, List<Map<Local, Object>> result) {
		final var successors = jProgram.getSuccsOf(thisUnit);
		assert successors.size() <= 1;
		if (successors.isEmpty()) {
			result.add(solveCurrentConstraints());
		} else {
			runJStmt(successors.get(0), result);
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
			case Local jVar -> symbolTable.get(jVar);
			case Constant c -> mapConst(jValue, c);
			case AbstractBinopExpr abe -> mapBinary(abe);
			case JVirtualInvokeExpr vie -> {
				var method = vie.getMethodRef();
				if ("equals".equals(method.getName())) {
					var lhs = vie.getBase();
					var rhs = vie.getArg(0);
					yield z3.mkEq(map(lhs), map(rhs));
				}
				todo(vie);
				yield null;
			}
			case JDynamicInvokeExpr x -> {
				var method = x.getMethodRef();
				// String concatenation.
				if ("makeConcatWithConstants".equals(method.getName())) {
					if (method.getParameterType(0) instanceof RefType t) {
						if (t.getClassName().equals(String.class.getName())) {
							// fixme
							// String / StringLiteral live in different argument boxes. We
							// presumed that there is one var and one literal, e.g.
							// "1".equals(str). But we can also compare two vars / two
							// literals (why?).
							var arr = new Expr[2];
							arr[0] = map(x.getArg(0));
							arr[1] = map(x.getBootstrapArg(0));
							yield z3.mkConcat(arr);
						}
					}
				}
				todo(x);
				yield null;
			}
			case JArrayRef x -> {
				var arrSymbol = symbolTable.get(x.getBase());
				yield z3.mkAt(arrSymbol, mapConst(null, (Constant) x.getIndex()));
			}
			case JCastExpr x -> map(x.getOp());
			default -> todo(jValue);
		};
	}

	private Expr mapBinary(AbstractBinopExpr abe) {
		var lhs = abe.getOp1();
		var rhs = abe.getOp2();
		return switch (abe) {
			case JAndExpr x -> z3.mkAnd(map(lhs), map(rhs));
			case JOrExpr x -> z3.mkOr(map(lhs), map(rhs));
			case JXorExpr x -> z3.mkXor(map(lhs), map(rhs));
			case JAddExpr x -> z3.mkAdd(map(lhs), map(rhs));
			case JSubExpr x -> z3.mkSub(map(lhs), map(rhs));
			case JMulExpr x -> z3.mkMul(map(lhs), map(rhs));
			case JDivExpr x -> z3.mkDiv(map(lhs), map(rhs));
			case JRemExpr x -> z3.mkRem(map(lhs), map(rhs));
			case JCmpExpr x -> z3.mkSub(map(lhs), map(rhs));
			case JCmplExpr x -> z3.mkSub(map(lhs), map(rhs));
			case JCmpgExpr x -> z3.mkSub(map(lhs), map(rhs));
			// These expressions appear exclusively in If Statements. CmpXXX ones
			// are more general.
			// Ref: http://www.sable.mcgill.ca/listarchives/soot-list/msg01032.html
			case JGeExpr x -> z3.mkGe(map(lhs), map(rhs));
			case JLeExpr x -> z3.mkLe(map(lhs), map(rhs));
			case JGtExpr x -> z3.mkGt(map(lhs), map(rhs));
			case JLtExpr x -> z3.mkLt(map(lhs), map(rhs));
			case JEqExpr x -> {
				var lhsSymbol = map(lhs);
				var rhsSymbol = map(rhs);
				if (lhsSymbol.isBool() && rhsSymbol.isInt()) {
					lhsSymbol = z3.mkITE(lhsSymbol, z3.mkInt(1), z3.mkInt(0));
				}
				yield z3.mkEq(lhsSymbol, rhsSymbol);
			}
			case JNeExpr x -> z3.mkNot(z3.mkEq(map(lhs), map(rhs)));
			default -> todo(abe);
		};
	}

	private Expr mapConst(Value jValue, Constant c) {
		return switch (c) {
			// A lot yet to do. E.g. NullConstant.
			case StringConstant x -> z3.mkString(x.value);
			case IntConstant x -> z3.mkInt(x.value);
			case DoubleConstant x -> mkReal(x.value);
			case FloatConstant x -> mkReal(x.value);
			default -> todo(jValue);
		};
	}

	/**
	 * Find concrete values of parameter jVars by interpreting the corresponding
	 * symbols.
	 */
	private Map<Local, Object> solveCurrentConstraints() {
		var status = solver.check();
		if (status != Status.SATISFIABLE) {
			return todo(status);
		}
		var model = solver.getModel();

		var result = new HashMap<Local, Object>();
		for (var kv: inputSymbolTable.entrySet()) {
			var jArgument = kv.getKey();
			var symbolicValue = kv.getValue();
			var interpretation = model.eval(symbolicValue, true);
			if (interpretation instanceof IntNum x) {
				result.put(jArgument, x.getInt());
			} else if (interpretation instanceof SeqExpr<?> x) {
				var sExpr = x.getSort().getSExpr();
				var sort = sExpr.substring(5, sExpr.length() - 1);
				if ("Int".equals(sort)) {
					var argSymbols = x.getArgs();
					var arr = new int[argSymbols.length];
					for (int i = 0; i < argSymbols.length; i++) {
						arr[i] = ((IntNum) argSymbols[i].getArgs()[0]).getInt();
					}
					result.put(jArgument, arr);
				} else {
					// Todo(Seideun): String?
					result.put(jArgument, x.getString());
				}
			} else {
				todo(interpretation);
			}
		}
		return result;
	}

	/* ****************** Utilities ****************** */

	private Expr mkRefConst(Local jVar, RefType x) {
		var className = x.getClassName();
		if (className.equals(String.class.getName())) {
			// Todo(Seideun): extract into a method
			return z3.mkConst(z3.mkSymbol(jVar.getName()), z3.mkStringSort());
		} else {
			return todo(x);
		}
	}

	private RealExpr mkReal(double x) {
		Rational rational = new Rational(x);
		return z3.mkReal(rational.numerator, rational.denominator);
	}

	private static <T> T todo(Object ignored) {
		try {
			throw new TodoException(ignored);
		} catch (TodoException e) {
			e.printStackTrace();
		}
		return null;
	}
}
