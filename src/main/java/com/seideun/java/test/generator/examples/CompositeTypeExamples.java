package com.seideun.java.test.generator.examples;

public class CompositeTypeExamples {

	public static int array(int[] a){
		if(a.length > 4){
			return a[0];
		}
		return 1;
	}

	public static void arrayStore(int[] a) {
		if (a.length > 1) {
			a[0] = 1;
		} else {
			a[0] = 0;
		}
	}

	public static void arrayStoreVar(int[] a, int i) {
		if (a.length > i) {
			a[i] = 1;
		} else {
			a[0] = 1;
		}
	}

	public static String StringTest(String a){
		return a;
	}
}
