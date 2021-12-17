package com.seideun.java.test_generator;

import com.microsoft.z3.Context;
import com.microsoft.z3.*;
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
		List<List<Unit>> paths = findPrimePaths(makeControlFlowGraph("twoBranches"
		));

	}

	@Test
	void convertSingleJExprToZ3Expr() {
		{
			JGeExpr input = new JGeExpr(
				new JimpleLocal("i", IntType.v()),
				IntConstant.v(1)
			);
			Expr<?> lhs = convertJExprToZ3Expr(input.getOp1());
			Expr<?> rhs = convertJExprToZ3Expr(input.getOp2());
			BoolExpr result = z3Context.mkGe((IntExpr) lhs, (IntExpr) rhs);

			assertEquals("(>= i 1)", result.toString());
		}
	}

	private Expr<?> convertJExprToZ3Expr(Value jValue) {
		if (jValue instanceof JimpleLocal) {
			JimpleLocal jimpleLocal = ((JimpleLocal) jValue);
			if (jimpleLocal.getType() == IntType.v()) {
				return z3Context.mkIntConst(jimpleLocal.getName());
			}
			throw new RuntimeException("todo");
		} else if (jValue instanceof IntConstant) {
			return z3Context.mkInt(((IntConstant) jValue).value);
		}
		throw new RuntimeException("todo");
	}
}
