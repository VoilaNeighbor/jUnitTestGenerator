package com.seideun.java.test_generator;

import java.util.List;

interface TestDataAssembler {
	List<TestDatum> assemble(Class<?> theClass, List<ArgumentList> argumentLists);
}
