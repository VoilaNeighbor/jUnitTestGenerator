package com.seideun.java.test.generator;

import com.seideun.java.test.generator.constriant_solver.JUnitTestGenerator;
import com.seideun.java.test.generator.constriant_solver.SootAgent;
import com.seideun.java.test.generator.constriant_solver.TestCase;
import com.seideun.java.test.generator.examples.BasicExamples;
import com.seideun.java.test.generator.symbolic_executor.JimpleConcolicMachine;
import lombok.AllArgsConstructor;
import soot.Local;
import soot.toolkits.graph.UnitGraph;

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

	public String makeTest(Class<?> theClass, String methodName) {
		try {
			UnitGraph controlFlowGraph = sootAgent.makeGraph(methodName);
			var argumentMaps = jimpleConcolicMachine.run(controlFlowGraph);
			var parameters = controlFlowGraph.getBody().getParameterLocals();

			List<TestCase> testCases = new ArrayList<>();
			for (Map<Local, Object> arguments: argumentMaps) {
				var input = parameters.stream().map(arguments::get).toList();
				var types = new Class<?>[input.size()];
				for (int i = 0; i < input.size(); i++) {
					types[i] = int.class;
				}
				var method = theClass.getMethod(methodName, types);
				var result = method.invoke(null, input.toArray());
				testCases.add(new TestCase(input, result));
			}
			return jUnitTestGenerator.generateAssertForEachCase(testCases);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		var sootAgent = SootAgent.basicExamples;
		var jcm = new JimpleConcolicMachine();
		var jUnitTestGenerator = new JUnitTestGenerator("myObject", "oneArg");
		var facade = new Facade(sootAgent, jUnitTestGenerator, jcm);
		System.out.println(facade.makeTest(BasicExamples.class, "oneArg"));
	}
}
