package com.seideun.java.test_generator;

import com.microsoft.z3.*;
import com.microsoft.z3.Context;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

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
	void singleIf() {
		List<List<Unit>> paths = findPrimePaths(makeControlFlowGraph("twoBranches"));

	}

	@Test
	void convertSingleJExprToZ3Expr() {
		{
			JGeExpr input = new JGeExpr(
				new JimpleLocal("i", IntType.v()),
				IntConstant.v(1)
			);
			IntExpr lhs = z3Context.mkIntConst("i");
			IntExpr rhs = z3Context.mkInt(1);
			BoolExpr result = z3Context.mkGe(lhs, rhs);

			assertEquals("(>= i 1)", result.toString());
		}
		{
			JGeExpr input = new JGeExpr(
				new JimpleLocal("x", IntType.v()),
				IntConstant.v(2)
			);
			IntExpr lhs = z3Context.mkIntConst("x");
			IntExpr rhs = z3Context.mkInt(2);
			BoolExpr result = z3Context.mkGe(lhs, rhs);

			assertEquals("(>= x 2)", result.toString());
		}
	}
}
