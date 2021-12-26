package com.seideun.java.test.generator.examples;

public final class JcmExamples {
	private JcmExamples() { }

	public static void empty() { }

	public static int intSequential(int a, int b) {
		return a + b;
	}

	public static int twoBranches(int a) {
		return a < 2 ? a : 1;
	}

	public static int manyIfs(int a, int b) {
		if (a < b) {
			return a + b;
		} else if (a == b + 2) {
			return a - b;
		} else {
			return a * b;
		}
	}

	public static double doubleType(double a, double b) {
		if (a < b) {
			return a + b;
		} else if (a + b > 3.9) {
			return a - b;
		} else {
			return a * b;
		}
	}

	public static String stringType(String a) {
		return "Hello " + a;
	}
}
