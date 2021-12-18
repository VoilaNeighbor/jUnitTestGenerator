package com.seideun.java.test_generator;

import com.microsoft.z3.Context;
import com.microsoft.z3.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.internal.*;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.*;
import java.util.stream.Collectors;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintConverterTest extends ConstraintConverter {
	static SootClass classUnderTest;

	@BeforeAll
	static void setupSoot() {
		final String rootClasspath =
			System.getProperty("user.dir") + "/target/test-classes";
		Options sootConfigs = Options.v();
		sootConfigs.set_prepend_classpath(true);
		sootConfigs.set_soot_classpath(rootClasspath);

		// Manually-loaded SootClass's are not set as app class by default.
		// I don't know of another way to load them yet. So let's bundle the setup
		// code as here.
		Scene sootScene = Scene.v();
		final String classname = "com.seideun.java.test_generator.ExampleCfgCases";
		classUnderTest = sootScene.loadClassAndSupport(classname);
		classUnderTest.setApplicationClass();
		sootScene.loadNecessaryClasses();
	}

	UnitGraph makeControlFlowGraph(String methodName) {
		SootMethod method = classUnderTest.getMethodByName(methodName);
		return new ExceptionalUnitGraph(method.retrieveActiveBody());
	}

	@Test
	@Disabled
	void seePath() {
		List<List<Unit>> path = findPrimePaths(makeControlFlowGraph(
			"twoBranches"));
	}

	@Test
	void convertJExprToZ3Expr() {
		JGeExpr input = new JGeExpr(
			new JimpleLocal("i", IntType.v()),
			IntConstant.v(1)
		);
		Expr<?> result = convertJValueToZ3Expr(input);
		assertEquals("(>= i 1)", result.toString());
	}

	@Test
	void findNameOfMethodParameters() {
		// How can we put a stub here?
		List<String> r1 = findNamesOfParameters(
			findPrimePaths(makeControlFlowGraph("oneArg")).get(0));
		List<String> r2 = findNamesOfParameters(
			findPrimePaths(makeControlFlowGraph("twoArgs")).get(0));
		List<String> r3 = findNamesOfParameters(
			findPrimePaths(makeControlFlowGraph("threeArgs")).get(0));

		assertEquals(Collections.singletonList("i0"), r1);
		assertEquals(Arrays.asList("i0", "i1"), r2);
		assertEquals(Arrays.asList("i0", "i1", "i2"), r3);
	}

	/**
	 * Collect constraint:
	 * - Every if condition on the path should be collected.
	 * - A condition should be negated if the JIfStmt evaluates to false.
	 * When a JIfStmt condition is true, the next node along the path will be
	 * its target.
	 * - Re-assigned variables should be distinguished from its old value. E.g.
	 * we should have x_0, x_1 ... for each def of x.
	 * - Conditions should be mapped to the SSA-ed form as well.
	 */

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

		List<Expr<?>> result = collectConstraints(input);

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

		List<Expr<?>> result = collectConstraints(input);

		assertEquals(constraintStrings.size(), result.size());
		assertTrue(constraintStrings.containsAll(
			result.stream()
				.map(Objects::toString)
				.collect(Collectors.toList())
		));
	}

	@Test
	void reassignedVariableDistinguished() {
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

		List<Expr<?>> result = collectConstraints(input);
		assertEquals(
			expected,
			result.stream()
				.map(Objects::toString)
				.collect(Collectors.toList())
		);
	}

}
