package com.seideun.java.test_generator;

import com.microsoft.z3.Context;
import com.microsoft.z3.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.ParameterRef;
import soot.jimple.internal.*;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.*;
import java.util.stream.Collectors;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
class ConstraintConverterTest {
	final Context z3Context = new Context();
	final Solver z3Solver = z3Context.mkSolver();
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
		Expr<?> result = convertJConstraintToZ3Expr(input);
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

	private List<Expr<?>> collectConstraints(List<Unit> path) {
		List<Expr<?>> result = new ArrayList<>();
		for (int i = 0, end = path.size() - 1; i != end; ++i) {
			Unit thisUnit = path.get(i);
			if (!(thisUnit instanceof JIfStmt)) { continue; }
			JIfStmt jIfStmt = (JIfStmt) thisUnit;
			Expr<BoolSort> z3Expr =
				(Expr<BoolSort>) convertJConstraintToZ3Expr(jIfStmt.getCondition());
			if (conditionIsTrue(jIfStmt, i, path)) {
				result.add(z3Expr);
			} else {
				result.add(z3Context.mkNot(z3Expr));
			}
		}
		return result;
	}

	private boolean conditionIsTrue(
		JIfStmt node,
		int indexOfNode,
		List<Unit> path
	) {
		return node.getTarget() == path.get(indexOfNode + 1);
	}

	private Expr<?> convertJConstraintToZ3Expr(Value jValue) {
		if (jValue instanceof JimpleLocal) {
			JimpleLocal jimpleLocal = ((JimpleLocal) jValue);
			if (jimpleLocal.getType() == IntType.v()) {
				return z3Context.mkIntConst(jimpleLocal.getName());
			}
			throw new RuntimeException("todo");
		} else if (jValue instanceof IntConstant) {
			return z3Context.mkInt(((IntConstant) jValue).value);
		} else if (jValue instanceof JGeExpr) {
			JGeExpr jGeExpr = (JGeExpr) jValue;
			return z3Context.mkGe(
				(Expr<? extends ArithSort>) convertJConstraintToZ3Expr(jGeExpr.getOp1()),
				(Expr<? extends ArithSort>) convertJConstraintToZ3Expr(jGeExpr.getOp2())
			);
		}
		throw new RuntimeException("todo");
	}

	private static List<String> findNamesOfParameters(List<Unit> input) {
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
