package com.seideun.java.test.generator;

import com.seideun.java.test.generator.constriant_solver.JUnitTestGenerator;
import com.seideun.java.test.generator.constriant_solver.PathArgumentsSynthesizer;
import com.seideun.java.test.generator.constriant_solver.SootAgent;
import com.seideun.java.test.generator.constriant_solver.TestCase;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer.findPrimePaths;

/**
 * Facade of the Java Unit Test Generator.
 */
public class Facade {
	private final SootAgent sootAgent;
	private final PathArgumentsSynthesizer pathArgumentsSynthesizer;
	private final JUnitTestGenerator jUnitTestGenerator;

	public Facade(
		SootAgent sootAgent,
		PathArgumentsSynthesizer pathArgumentsSynthesizer,
		JUnitTestGenerator jUnitTestGenerator
	) {
		this.sootAgent = sootAgent;
		this.pathArgumentsSynthesizer = pathArgumentsSynthesizer;
		this.jUnitTestGenerator = jUnitTestGenerator;
	}

	public String makeTest(Class<?> theClass, String methodName) {
		// Needs rework.
		try {
			List<TestCase> testCases = new ArrayList<>();
			UnitGraph controlFlowGraph = sootAgent.makeControlFlowGraph(methodName);
			for (List<Unit> path: findPrimePaths(controlFlowGraph)) {
				pathArgumentsSynthesizer.store(path);
				Optional<List<Object>> synthesizeResult =
					pathArgumentsSynthesizer.synthesizeArguments();
				if (!synthesizeResult.isPresent()) {
					continue;
				}
				List<Object> arguments = synthesizeResult.get();
				Class<?>[] argumentClasses = new Class<?>[arguments.size()];
				for (int i = 0; i != arguments.size(); ++i) {
					// argumentClasses[i] = arguments.get(i).getClass();
					argumentClasses[i] = int.class;
				}
				Object expectedOutput = theClass.getMethod(methodName, argumentClasses)
					.invoke(null, (int)arguments.get(0));
				testCases.add(new TestCase(arguments, expectedOutput));
			}
			return jUnitTestGenerator.generateAssertForEachCase(testCases);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		SootAgent sootAgent = new SootAgent(ExampleCfgCases.class);
		PathArgumentsSynthesizer argumentsSynthesizer =
			new PathArgumentsSynthesizer();
		JUnitTestGenerator jUnitTestGenerator = new JUnitTestGenerator(
			ExampleCfgCases.class.getSimpleName().toLowerCase(Locale.ROOT),
			"jumpBackToLoopEntrance"
		);
		Facade facade = new Facade(
			sootAgent,
			argumentsSynthesizer,
			jUnitTestGenerator
		);
		System.out.println(facade.makeTest(ExampleCfgCases.class, "jumpBackToLoopEntrance"));
	}
}
