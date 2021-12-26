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

	public static int whileLoop(int a){
		while (a > 10) {
			a -= 2;
		}
		return a;
	}

	public static String stringType(String a) {
		return "Hello " + a;
	}

	public static String stringEquals(String a, String b) {
		if (a.equals(b)) {
			return a + b;
		} else {
			return a;
		}
	}
}
