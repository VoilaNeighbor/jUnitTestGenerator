package com.seideun.java.test_generator;

/**
 * Example methods for tests on control-flow-graph analyzers.
 */
final class ExampleCfgCases {
	public static int oneArg(int a) {
		return a * 2;
	}

	public static int twoArgs(int a, int b) {
		return a + b;
	}

	public static int threeArgs(int a, int b, int c) {
		return a + b * c;
	}

	public static int sequential(int a) {
		return 3 * a;
	}

	public static int twoBranches(int a) {
		return a < 1 ? 9 : 6;
	}

	public static int jumpBackToLoopEntrance(int a) {
		while (a < 20) {
			a *= 2;
		}
		return a;
	}
}
