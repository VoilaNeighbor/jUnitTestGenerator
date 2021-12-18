package com.seideun.java.test_generator;

import com.microsoft.z3.*;
import soot.IntType;
import soot.Unit;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.ParameterRef;
import soot.jimple.internal.*;

import java.util.*;

/**
 * This class converts constraints in the form of Jimple ASTs to Z3 expressions.
 */
@SuppressWarnings("unchecked")
public class ConstraintSolver {
	private final Context z3Context = new Context();
	private final Map<JimpleLocal, Integer> timesLocalsAssigned = new HashMap<>();
	private final List<Expr<?>> constraints = new ArrayList<>();
	private final List<String> jNamesOfArgs = new ArrayList<>();

	/**
	 * Jimple swipes out the original names of the method, replacing with i0, i1
	 * etc. Besides, there are locals in the Units. This method sorts out the
	 * arguments from the input, and return their names in Jimple.
	 */
	public static List<String> findJNamesOfArgs(Iterable<Unit> path) {
		List<String> result = new ArrayList<>();
		findJNamesOfArgs(path, result);
		return result;
	}

	/**
	 * Store the parameters and constraints along the path.
	 *
	 * It is possible to store multiple paths. This class will try to solve all
	 * constraints on the paths together.
	 */
	public void storePath(List<Unit> path) {
		findJNamesOfArgs(path, jNamesOfArgs);
		// JIfStmts and JGotoStmts are guaranteed to have nodes following them.
		for (int i = 0, end = path.size() - 1; i != end; ++i) {
			Unit thisUnit = path.get(i);
			if (thisUnit instanceof JAssignStmt) {
				incrementTimesAssigned((JAssignStmt) thisUnit);
			} else if (thisUnit instanceof JIfStmt) {
				constraints.add(extractConstraintOf((JIfStmt) thisUnit, i, path));
			}
		}
	}

	public void solveConstraints() {
		Solver solver = z3Context.mkSolver();
		Status status = solver.check();
		Model model = solver.getModel();
	}

	public List<Expr<?>> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	protected Expr<?> convertJValueToZ3Expr(Value jValue) {
		if (jValue instanceof JimpleLocal) {
			JimpleLocal jimpleLocal = ((JimpleLocal) jValue);
			if (jimpleLocal.getType() == IntType.v()) {
				Integer timesReassigned = timesLocalsAssigned.get(jimpleLocal);
				String name = timesReassigned == null
					? jimpleLocal.getName()
					: (jimpleLocal.getName() + "$" + timesReassigned);
				return z3Context.mkIntConst(name);
			}
			throw new TodoException();
		} else if (jValue instanceof IntConstant) {
			return z3Context.mkInt(((IntConstant) jValue).value);
		} else if (jValue instanceof JGeExpr) {
			JGeExpr jGeExpr = (JGeExpr) jValue;
			return z3Context.mkGe(
				(Expr<? extends ArithSort>) convertJValueToZ3Expr(
					jGeExpr.getOp1()
				),
				(Expr<? extends ArithSort>) convertJValueToZ3Expr(
					jGeExpr.getOp2()
				)
			);
		} else {
			throw new TodoException();
		}
	}

	private Expr<BoolSort> extractConstraintOf(
		JIfStmt theIf, int idxInPath, List<Unit> path
	) {
		Expr<BoolSort> z3Expr = (Expr<BoolSort>)
			convertJValueToZ3Expr(theIf.getCondition());
		return theIf.getTarget() != path.get(idxInPath + 1)
			? z3Context.mkNot(z3Expr)
			: z3Expr;
	}

	private static void findJNamesOfArgs(Iterable<Unit> path, List<String> out) {
		for (Unit unit: path) {
			if (unit instanceof JIdentityStmt) {
				JIdentityStmt jIdentityStmt = (JIdentityStmt) unit;
				if (jIdentityStmt.getRightOp() instanceof ParameterRef) {
					out.add(jIdentityStmt.getLeftOp().toString());
				}
			}
		}
	}

	private void incrementTimesAssigned(JAssignStmt jAssignStmt) {
		JimpleLocal local = (JimpleLocal) jAssignStmt.getLeftOp();
		timesLocalsAssigned.compute(local, (k, v) -> 1 + (v == null ? 0 : v));
	}
}
