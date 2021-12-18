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
 * I'm a bridge connecting Soot with Z3.
 * <p>
 * I convert constraints in the form of Jimple ASTs to Z3 expressions.
 */
@SuppressWarnings("unchecked")
public class ConstraintSolver {
	private final Context z3Context = new Context();
	private final Map<JimpleLocal, Integer> timesLocalsAssigned = new HashMap<>();
	private final List<Expr<?>> constraints = new ArrayList<>();
	private final List<JimpleLocal> jMethodArgs = new ArrayList<>();

	/**
	 * Store the parameters and constraints along the path.
	 * <p>
	 * It is possible to store multiple paths. This class will try to solve all
	 * constraints on the paths together.
	 */
	public void storePath(List<Unit> path) {
		collectArgs(path);
		collectConstraints(path);
	}

	public List<Expr<?>> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	public List<Object> synthesizeArguments() {
		Solver solver = z3Context.mkSolver();
		Model model = solver.getModel();
		List<Object> result = new ArrayList<>();
		return result;
	}

	protected Expr<?> convertJValueToZ3Expr(Value jValue) {
		if (jValue instanceof JimpleLocal) {
			JimpleLocal jimpleLocal = ((JimpleLocal) jValue);
			Integer timesReassigned = timesLocalsAssigned.get(jimpleLocal);
			String postfix = timesReassigned == null ? "" : ("$" + timesReassigned);
			if (jimpleLocal.getType() == IntType.v()) {
				return z3Context.mkIntConst(jimpleLocal.getName() + postfix);
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
		} else if (jValue instanceof JLtExpr) {
			JLtExpr jGeExpr = (JLtExpr) jValue;
			return z3Context.mkLt(
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

	/**
	 * Jimple swipes out the original names of the method, replacing with i0, i1
	 * etc. Besides, there are locals in the Units. This method sorts out the
	 * arguments from the input, and return their names in Jimple.
	 */
	private void collectArgs(Iterable<Unit> path) {
		for (Unit unit: path) {
			if (unit instanceof JIdentityStmt) {
				JIdentityStmt jIdentityStmt = (JIdentityStmt) unit;
				if (jIdentityStmt.getRightOp() instanceof ParameterRef) {
					jMethodArgs.add(((JimpleLocal) jIdentityStmt.getLeftOp()));
				}
			}
		}
	}

	private void collectConstraints(List<Unit> path) {
		// JIfStmts and JGotoStmts are guaranteed to have nodes following them.
		// So we do not have to check the last unit.
		for (int i = 0, end = path.size() - 1; i != end; ++i) {
			Unit thisUnit = path.get(i);
			if (thisUnit instanceof JAssignStmt) {
				incrementTimesAssigned((JAssignStmt) thisUnit);
			} else if (thisUnit instanceof JIfStmt) {
				constraints.add(extractConstraintOf((JIfStmt) thisUnit, i, path));
			}
		}
	}

	private void incrementTimesAssigned(JAssignStmt jAssignStmt) {
		JimpleLocal local = (JimpleLocal) jAssignStmt.getLeftOp();
		timesLocalsAssigned.compute(local, (k, v) -> 1 + (v == null ? 0 : v));
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
}
