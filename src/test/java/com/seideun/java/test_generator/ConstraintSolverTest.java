package com.seideun.java.test_generator;

import com.microsoft.z3.Expr;
import org.junit.jupiter.api.Test;
import soot.DoubleType;
import soot.IntType;
import soot.Unit;
import soot.jimple.DoubleConstant;
import soot.jimple.IntConstant;
import soot.jimple.ParameterRef;
import soot.jimple.internal.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Todo(Seideun):
 *   - Can get input suite.
 *   - Member access of class.
 */
class ConstraintSolverTest extends ConstraintSolver {
	@Test
	void collectAsIsIfConditionTrue() {
		Unit dummy = new JReturnVoidStmt();
		List<Unit> input = new ArrayList<>();
		input.add(new JIfStmt(
			new JGeExpr(new JimpleLocal("i", IntType.v()), IntConstant.v(1)), dummy
		));
		input.add(dummy);
		input.add(new JIfStmt(
			new JGeExpr(new JimpleLocal("x", IntType.v()), IntConstant.v(2)), dummy
		));
		input.add(dummy);
		Set<String> constraintStrings = new HashSet<>();
		constraintStrings.add("(>= i 1)");
		constraintStrings.add("(>= x 2)");

		storePath(input);
		List<Expr<?>> result = getConstraints();

		assertEquals(constraintStrings.size(), result.size());
		assertTrue(constraintStrings.containsAll(
			result.stream()
				.map(Objects::toString)
				.collect(Collectors.toList())
		));
	}

	@Test
	void negateConstraintIfConditionFalse() {
		Unit dummy = new JReturnVoidStmt();
		List<Unit> input = new ArrayList<>();
		input.add(new JIfStmt(
			new JGeExpr(new JimpleLocal("i", IntType.v()), IntConstant.v(1)), dummy
		));
		input.add(new JIfStmt(
			new JGeExpr(new JimpleLocal("x", IntType.v()), IntConstant.v(2)), dummy
		));
		input.add(dummy);
		Set<String> constraintStrings = new HashSet<>();
		constraintStrings.add("(not (>= i 1))");
		constraintStrings.add("(>= x 2)");

		storePath(input);
		List<Expr<?>> result = getConstraints();

		assertEquals(constraintStrings.size(), result.size());
		assertTrue(constraintStrings.containsAll(
			result.stream()
				.map(Objects::toString)
				.collect(Collectors.toList())
		));
	}

	@Test
	void reassignedVariableHasNewName() {
		JimpleLocal x = new JimpleLocal("x", IntType.v());
		JAssignStmt firstAssign = new JAssignStmt(x, IntConstant.v(3));
		JAssignStmt secondAssign = new JAssignStmt(x, IntConstant.v(2));
		JGeExpr condition = new JGeExpr(x, IntConstant.v(1));
		JReturnVoidStmt sink = new JReturnVoidStmt();
		JIfStmt firstBranch = new JIfStmt(condition, sink);
		JIfStmt secondBranch = new JIfStmt(condition, sink);
		JIfStmt thirdBranch = new JIfStmt(condition, sink);

		List<Unit> input = new ArrayList<>();
		input.add(firstBranch);
		input.add(firstAssign);
		input.add(secondBranch);
		input.add(secondAssign);
		input.add(thirdBranch);
		input.add(sink);

		List<String> expected = new ArrayList<>();
		expected.add("(not (>= x 1))");
		expected.add("(not (>= x$1 1))");
		expected.add("(>= x$2 1)");

		storePath(input);
		List<Expr<?>> result = getConstraints();
		assertEquals(
			expected,
			result.stream()
				.map(Objects::toString)
				.collect(Collectors.toList())
		);
	}

	@Test
	void canStoreMultiplePaths() {
		JimpleLocal x = new JimpleLocal("x", IntType.v());
		JAssignStmt firstAssign = new JAssignStmt(x, IntConstant.v(3));
		JAssignStmt secondAssign = new JAssignStmt(x, IntConstant.v(2));
		JGeExpr condition = new JGeExpr(x, IntConstant.v(1));
		JReturnVoidStmt sink = new JReturnVoidStmt();
		JIfStmt firstBranch = new JIfStmt(condition, sink);
		JIfStmt secondBranch = new JIfStmt(condition, sink);
		JIfStmt thirdBranch = new JIfStmt(condition, sink);

		List<Unit> input = new ArrayList<>();
		input.add(firstBranch);
		input.add(firstAssign);
		input.add(secondBranch);
		input.add(secondAssign);
		input.add(thirdBranch);
		input.add(sink);

		List<String> expected = new ArrayList<>();
		expected.add("(not (>= x 1))");
		expected.add("(not (>= x$1 1))");
		expected.add("(>= x$2 1)");
		expected.add("(not (>= x$2 1))");
		expected.add("(not (>= x$3 1))");
		expected.add("(>= x$4 1)");

		storePath(input);
		storePath(input);
		List<Expr<?>> result = getConstraints();

		assertEquals(
			expected,
			result.stream()
				.map(Objects::toString)
				.collect(Collectors.toList())
		);
	}

	@Test
	void canSynthesizeInput() {
		JimpleLocal y = new JimpleLocal("y", DoubleType.v());
		JGeExpr geConstraint = new JGeExpr(y, DoubleConstant.v(2.33));
		JLtExpr ltConstraint = new JLtExpr(y, DoubleConstant.v(2.34));
		JReturnVoidStmt sink = new JReturnVoidStmt();
		JIdentityStmt parameter = new JIdentityStmt(
			y,
			new ParameterRef(IntType.v(), 0)
		);

		List<Unit> input = new ArrayList<>();
		input.add(parameter);
		input.add(new JIfStmt(geConstraint, sink));
		input.add(sink);
		input.add(new JIfStmt(ltConstraint, sink));
		input.add(sink);

		storePath(input);
		List<Object> result = synthesizeArguments().get();
		double arg = (double) result.get(0);

		assertEquals(1, result.size());
		assertTrue(arg >= 2.33);
		assertTrue(arg < 2.34);
	}
}
