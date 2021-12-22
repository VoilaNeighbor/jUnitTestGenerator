package com.seideun.java.test_generator;

import soot.toolkits.graph.UnitGraph;

import java.util.List;

public class JUnitTestGenerator {
	private final ControlFlowGraphMaker cfgMaker;
	private final PrimePathFinder primePathFinder;
	private final ConstraintSolver solver;
	private final TestDataAssembler testDataAssembler;
	private final JUnitAssembler jUnitAssembler;

	public static class Builder {
		private ControlFlowGraphMaker cfgMaker;
		private PrimePathFinder primePathFinder;
		private ConstraintSolver constraintSolver;
		private TestDataAssembler testDataAssembler;
		private JUnitAssembler jUnitAssembler;

		public Builder setCfgMaker(ControlFlowGraphMaker cfgMaker) {
			this.cfgMaker = cfgMaker;
			return this;
		}

		public Builder setPrimePathFinder(PrimePathFinder primePathFinder) {
			this.primePathFinder = primePathFinder;
			return this;
		}

		public Builder setConstraintSolver(ConstraintSolver constraintSolver) {
			this.constraintSolver = constraintSolver;
			return this;
		}

		public Builder setTestDataAssembler(TestDataAssembler testDataAssembler) {
			this.testDataAssembler = testDataAssembler;
			return this;
		}

		public Builder setJUnitAssembler(JUnitAssembler jUnitAssembler) {
			this.jUnitAssembler = jUnitAssembler;
			return this;
		}

		public JUnitTestGenerator build() {
			return new JUnitTestGenerator(
				cfgMaker,
				primePathFinder,
				constraintSolver,
				testDataAssembler,
				jUnitAssembler
			);
		}
	}

	private JUnitTestGenerator(
		ControlFlowGraphMaker cfgMaker,
		PrimePathFinder primePathFinder,
		ConstraintSolver constraintSolver,
		TestDataAssembler testDataAssembler,
		JUnitAssembler jUnitAssembler
	) {
		this.cfgMaker = cfgMaker;
		this.primePathFinder = primePathFinder;
		this.solver = constraintSolver;
		this.testDataAssembler = testDataAssembler;
		this.jUnitAssembler = jUnitAssembler;
	}

	public String makeJUnitTest(Class<?> theClass, String methodName) {
		UnitGraph cfg = cfgMaker.makeControlFlowGraph(theClass, methodName);
		List<Path> paths = primePathFinder.findPrimePaths(cfg);
		List<ArgumentList> arguments = solver.makeSatisfiableInput(paths);
		List<TestDatum> testData = testDataAssembler.assemble(theClass, arguments);
		return jUnitAssembler.assembleTestMethod(methodName, testData);
	}
}

