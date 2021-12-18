package com.seideun.java.test_generator;

import com.microsoft.z3.*;
import soot.DoubleType;
import soot.IntType;
import soot.Unit;
import soot.Value;
import soot.jimple.DoubleConstant;
import soot.jimple.IntConstant;
import soot.jimple.ParameterRef;
import soot.jimple.RealConstant;
import soot.jimple.internal.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * I'm a bridge connecting Soot with Z3.
 * <p>
 * I convert constraints in the form of Jimple ASTs to Z3 expressions.
 */
@SuppressWarnings("unchecked")
public class ConstraintSolver {
	private final Context z3Context = new Context();
	private final Map<JimpleLocal, Integer> timesLocalsAssigned = new HashMap<>();
	private final List<BoolExpr> constraints = new ArrayList<>();
	private List<Expr<?>> methodArgs;

	/**
	 * Store the parameters and constraints along the path.
	 * <p>
	 * It is possible to store multiple paths. This class will try to solve all
	 * constraints on the paths together.
	 */
	public void storePath(List<Unit> path) {
		// JIfStmts and JGotoStmts are guaranteed to have nodes following them.
		if (path.get(path.size() - 1) instanceof JIfStmt) {
			throw new IllFormedJIfStmtException(path);
		}
		collectArgs(path);
		collectConstraints(path);
	}

	public List<Expr<?>> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	public Optional<List<Object>> synthesizeArguments() {
		Solver solver = z3Context.mkSolver();
		solver.add(constraints.toArray(new BoolExpr[0]));
		if (solver.check() != Status.SATISFIABLE) {
			return Optional.empty();
		}
		Model model = solver.getModel();
		List<Object> result = new ArrayList<>();
		for (Expr<?> arg: methodArgs) {
			Expr<?> value = model.eval(arg, false);
			if (value instanceof IntNum) {
				result.add(((IntNum) value).getInt());
			} else if (value instanceof RatNum) {
				RatNum ratNum = (RatNum) value;
				result.add((double) ratNum.getNumerator().getInt64() /
					(double) ratNum.getDenominator().getInt64());
			} else if (value instanceof IntExpr) {
				result.add(0);
			} else if (value instanceof RealExpr) {
				result.add(0.0);
			} else {
				throw new TodoException(value);
			}
		}
		return Optional.of(result);
	}

	private void collectArgs(List<Unit> path) {
		methodArgs = path.stream()
			.filter(JIdentityStmt.class::isInstance)
			.map(JIdentityStmt.class::cast)
			.filter(x -> x.getRightOp() instanceof ParameterRef)
			.map(x -> convertJValueToZ3Expr(x.getLeftOp()))
			.collect(Collectors.toList());
	}

	private void collectConstraints(List<Unit> path) {
		for (int i = 0, end = path.size() - 1; i != end; ++i) {
			Unit thisUnit = path.get(i);
			if (thisUnit instanceof JAssignStmt) {
				incrementTimesAssigned(((JAssignStmt) thisUnit).getLeftOp());
			} else if (thisUnit instanceof JIfStmt) {
				constraints.add(extractConstraintOf((JIfStmt) thisUnit, i, path));
			}
		}
	}

	private void incrementTimesAssigned(Value local) {
		timesLocalsAssigned.compute(
			(JimpleLocal) local,
			(k, v) -> 1 + (v == null ? 0 : v)
		);
	}

	private BoolExpr extractConstraintOf(
		JIfStmt theIf, int idxInPath, List<Unit> path
	) {
		BoolExpr constraint =
			(BoolExpr) convertJValueToZ3Expr(theIf.getCondition());
		return theIf.getTarget() != path.get(idxInPath + 1)
			? z3Context.mkNot(constraint)
			: constraint;
	}

	private Expr<?> convertJValueToZ3Expr(Value jValue) {
		if (jValue instanceof JimpleLocal) {
			JimpleLocal jimpleLocal = ((JimpleLocal) jValue);
			Integer timesReassigned = timesLocalsAssigned.get(jimpleLocal);
			String varName = jimpleLocal.getName() +
				(timesReassigned == null ? "" : ("$" + timesReassigned));
			if (jimpleLocal.getType() == IntType.v()) {
				return z3Context.mkIntConst(varName);
			} else if (jimpleLocal.getType() == DoubleType.v()) {
				return z3Context.mkRealConst(varName);
			}
			throw new TodoException();
		} else if (jValue instanceof IntConstant) {
			return z3Context.mkInt(((IntConstant) jValue).value);
		} else if (jValue instanceof RealConstant) {
			Rational rational = new Rational(((DoubleConstant) jValue).value);
			return z3Context.mkReal(rational.numerator, rational.denominator);
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
}
