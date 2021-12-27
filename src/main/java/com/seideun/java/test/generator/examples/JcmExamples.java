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

	public static int twoWhileLoops(int a) {
		while (a > 11) {
			a -= 13;
		}
		a += 1;
		while (a < 7) {
			a += 3;
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

	public static int arrayType(int[] a) {
		return a[0];
	}

	public static void arrayAssign(int[] a, int b) {
		a[1] = b;
	}
}
