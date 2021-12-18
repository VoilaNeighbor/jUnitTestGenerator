package com.seideun.java.test_generator;

import static com.google.common.math.IntMath.gcd;

public class Rational {
	public final int numerator;
	public final int denominator;

	public Rational(double doubleNum) {
		String s = String.valueOf(doubleNum);
		int digitsDec = s.length() - 1 - s.indexOf('.');
		int d = 1;
		for (int i = 0; i < digitsDec; i++) {
			doubleNum *= 10;
			d *= 10;
		}
		int num = (int) Math.round(doubleNum);
		int g = gcd(num, d);
		numerator = num / g;
		denominator = d / g;
	}
}
