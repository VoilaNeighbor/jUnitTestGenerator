package com.seideun.java.test_generator;

import com.google.common.collect.Lists;
import com.microsoft.z3.ArithSort;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.ParameterRef;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JimpleLocal;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
	void findNameOfMethodParameters() {
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

	private List<String> findNamesOfParameters(List<Unit> input) {
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

	@Test
	void convertJExprToZ3Expr() {
		JGeExpr input = new JGeExpr(
			new JimpleLocal("i", IntType.v()),
			IntConstant.v(1)
		);
		Expr<?> result = convertJConstraintToZ3Expr(input);
		assertEquals("(>= i 1)", result.toString());
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
			//noinspection unchecked
			return z3Context.mkGe(
				(Expr<? extends ArithSort>) convertJConstraintToZ3Expr(jGeExpr.getOp1()),
				(Expr<? extends ArithSort>) convertJConstraintToZ3Expr(jGeExpr.getOp2())
			);
		}
		throw new RuntimeException("todo");
	}
}
