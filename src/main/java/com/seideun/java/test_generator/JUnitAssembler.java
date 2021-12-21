package com.seideun.java.test_generator;

import java.util.List;

interface JUnitAssembler {
	String makeTestMethod(String methodName, List<TestDatum> testData);
}
