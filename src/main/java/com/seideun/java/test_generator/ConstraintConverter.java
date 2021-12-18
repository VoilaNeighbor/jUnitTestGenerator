package com.seideun.java.test_generator;

import com.microsoft.z3.ArithSort;
import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
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
public class ConstraintConverter {
	private final Context z3Context = new Context();

	public List<Expr<?>> collectConstraints(List<Unit> path) {
		List<Expr<?>> result = new ArrayList<>();
		Map<JimpleLocal, Integer> timesLocalsAssigned = new HashMap<>();
		for (int i = 0, end = path.size() - 1; i != end; ++i) {
			Unit thisUnit = path.get(i);
			if (thisUnit instanceof JIfStmt) {
				JIfStmt jIfStmt = (JIfStmt) thisUnit;
				Expr<BoolSort> z3Expr = (Expr<BoolSort>)
					convertJValueToZ3Expr(jIfStmt.getCondition(), timesLocalsAssigned);
				if (conditionIsTrue(jIfStmt, i, path)) {
					result.add(z3Expr);
				} else {
					result.add(z3Context.mkNot(z3Expr));
				}
			} else if (thisUnit instanceof JAssignStmt) {
				JAssignStmt jAssignStmt = (JAssignStmt) thisUnit;
				JimpleLocal local = (JimpleLocal) jAssignStmt.getLeftOp();
				timesLocalsAssigned.compute(local, (_k, v) -> v == null ? 1 : v + 1);
			}
		}
		return result;
	}

	private static boolean conditionIsTrue(
		JIfStmt node,
		int indexOfNode,
		List<Unit> path
	) {
		return node.getTarget() == path.get(indexOfNode + 1);
	}

	public Expr<?> convertJValueToZ3Expr(Value jValue) {
		return convertJValueToZ3Expr(jValue, Collections.emptyMap());
	}

	/**
	 * @param timesLocalsAssigned Used for name remapping
	 */
	private Expr<?> convertJValueToZ3Expr(
		Value jValue,
		Map<JimpleLocal, Integer> timesLocalsAssigned
	) {
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
					jGeExpr.getOp1(),
					timesLocalsAssigned
				),
				(Expr<? extends ArithSort>) convertJValueToZ3Expr(
					jGeExpr.getOp2(),
					timesLocalsAssigned
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
	public static List<String> findNamesOfParameters(Collection<Unit> input) {
		List<String> result = new ArrayList<>();
		for (Unit unit: input) {
			if (unit instanceof JIdentityStmt) {
				JIdentityStmt jIdentityStmt = (JIdentityStmt) unit;
				if (jIdentityStmt.getRightOp() instanceof ParameterRef) {
					result.add(jIdentityStmt.getLeftOp().toString());
				}
			}
		}
		return result;
	}
}
