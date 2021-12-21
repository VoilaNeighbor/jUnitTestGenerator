package com.seideun.java.test.generator.constraint_solver;

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

	public static int redefinition(int a) {
		int b = a * 2;
		b -= 1;
		b += 5;
		return b;
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

	public static int boolConnective(int a, int b) {
		if (a < 2 && b > 5) {
			return a + b;
		} else {
			return a - b;
		}
	}

}