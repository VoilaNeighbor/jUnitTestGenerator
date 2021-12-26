package com.seideun.java.test.generator;

import com.seideun.java.test.generator.constriant_solver.JUnitTestGenerator;
import com.seideun.java.test.generator.constriant_solver.PathArgumentsSynthesizer;
import com.seideun.java.test.generator.constriant_solver.SootAgent;
import com.seideun.java.test.generator.examples.BasicExamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Needs rework.
@Disabled
class FacadeTest {
	static final String ARBITRARY_METHOD_NAME = "twoBranjumpBackToLoopEntranceches";
	final SootAgent sootAgent = spy(SootAgent.basicExamples);
	final PathArgumentsSynthesizer argumentsSynthesizer =
		mock(PathArgumentsSynthesizer.class);
	final JUnitTestGenerator jUnitTestGenerator =
		mock(JUnitTestGenerator.class);

	@BeforeEach
	void setup() {
	}

	@Test
	void canGenerateJUnitTestGivenPathToCompiledMethod() {
		Facade facade = new Facade(sootAgent, argumentsSynthesizer, jUnitTestGenerator);
		facade.makeTest(BasicExamples.class, ARBITRARY_METHOD_NAME);
		verify(sootAgent, atLeastOnce()).makeGraph(any());
		verify(argumentsSynthesizer, atLeastOnce()).synthesizeArguments();
		verify(jUnitTestGenerator, atLeastOnce()).generateAssertForEachCase(any());
	}
}
