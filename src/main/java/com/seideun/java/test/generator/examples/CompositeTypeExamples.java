package com.seideun.java.test.generator.examples;

public class CompositeTypeExamples {

	public static int array(int[] a){
		if(a.length > 4){
			return a[0];
		}
		return 1;
	}

	public static String StringTest(String a){
		return a;
	}
}
