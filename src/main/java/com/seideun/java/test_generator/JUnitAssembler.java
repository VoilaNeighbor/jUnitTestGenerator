package com.seideun.java.test_generator;

import java.util.List;

interface JUnitAssembler {
	String assembleTestMethod(String methodName, List<TestDatum> testData);
}
