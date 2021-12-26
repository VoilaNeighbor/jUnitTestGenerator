package com.seideun.java.test.generator.examples;

public final class JcmExamples {
	private JcmExamples() { }

	public static void empty() { }

	public static int intSequential(int a, int b) {
		return a + b;
	}

	public static int twoBranches(int a) {
		return a < -2 ? a - 1 : a + 1;
	}

	public static String stringType(String a) {
		return "Hello " + a;
	}
}
