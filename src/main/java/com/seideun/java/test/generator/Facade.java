package com.seideun.java.test.generator;

import com.seideun.java.test.generator.constriant_solver.JUnitTestGenerator;
import com.seideun.java.test.generator.constriant_solver.SootAgent;
import com.seideun.java.test.generator.constriant_solver.TestCase;
import com.seideun.java.test.generator.examples.BasicExamples;
import com.seideun.java.test.generator.symbolic_executor.JimpleConcolicMachine;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import soot.Local;
import soot.toolkits.graph.UnitGraph;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Facade of the Java Unit Test Generator.
 */
@AllArgsConstructor
public class Facade {
	private final SootAgent sootAgent;
	private final JUnitTestGenerator jUnitTestGenerator;
	private final JimpleConcolicMachine jimpleConcolicMachine;

	public void makeTest(Class<?> theClass, String methodName) {
		UnitGraph controlFlowGraph = sootAgent.makeGraph(methodName);
		var argumentMaps = jimpleConcolicMachine.run(controlFlowGraph);
		var parameters = controlFlowGraph.getBody().getParameterLocals();
		makeTestCases(theClass, methodName, argumentMaps, parameters);
	}

	@SneakyThrows
	private void makeTestCases(
		Class<?> theClass,
		String methodName,
		List<Map<Local, Object>> argumentMaps,
		List<Local> parameters
	) {
		List<TestCase> testCases = new ArrayList<>();
		for (Map<Local, Object> arguments: argumentMaps) {
			var input = parameters.stream().map(arguments::get).toList();
			var types = new Class<?>[input.size()];
			for (int i = 0; i < input.size(); i++) {
				if (input.get(i).getClass() == Integer.class) {
					types[i] = int.class;
				} else {
					types[i] = input.get(i).getClass();
				}
			}
			var method = theClass.getMethod(methodName, types);
			var result = method.invoke(null, input.toArray());
			testCases.add(new TestCase(input, result));
		}
		jUnitTestGenerator.addTestMethod(methodName, testCases);
	}

	@SneakyThrows
	public static void main(String[] args) {
		var sootAgent = SootAgent.basicExamples;
		var jcm = new JimpleConcolicMachine();
		var jUnitTestGenerator = new JUnitTestGenerator("myObject", "");
		var facade = new Facade(sootAgent, jUnitTestGenerator, jcm);

		facade.makeTest(BasicExamples.class, "twoArgs");
		facade.makeTest(BasicExamples.class, "twoBranches");
		try (var fileWriter = new FileWriter("generated_junit_tests/MyTest.java")) {
			jUnitTestGenerator.buildToWriter(fileWriter);
		}
	}
}
