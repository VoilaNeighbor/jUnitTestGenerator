package com.seideun.java.test_generator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class JUnitTestGeneratorTest {
	static final Logger LOGGER = LogManager.getLogger();
	static final String ROOT_OF_CLASS =
		System.getProperty("user.dir") + "/target/test-classes";
	static final Class<?> CLASS = ExampleInput.class;
	static final String METHOD_NAME = "twoBranches";
	ControlFlowGraphMaker controlFlowGraphMaker = mock(ControlFlowGraphMaker.class);
	ConstraintSolver constraintSolver = mock(ConstraintSolver.class);
	PrimePathFinder primePathFinder = mock(PrimePathFinder.class);
	TestDataAssembler testDataAssembler = mock(TestDataAssembler.class);
	JUnitAssembler jUnitAssembler = mock(JUnitAssembler.class);
	JUnitTestGenerator generator = new JUnitTestGenerator.Builder()
		.setCfgMaker(controlFlowGraphMaker)
		.setConstraintSolver(constraintSolver)
		.setPrimePathFinder(primePathFinder)
		.setTestDataAssembler(testDataAssembler)
		.setJUnitAssembler(jUnitAssembler)
		.build();

	@Test
	void primePathCoverageTest() {
		String result = generator.makeJUnitTest(CLASS, METHOD_NAME);
		LOGGER.info(result);
		// Todo(Seideun): Prime coverage analysis
	}
}
