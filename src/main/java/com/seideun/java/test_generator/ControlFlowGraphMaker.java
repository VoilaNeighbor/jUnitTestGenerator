package com.seideun.java.test_generator;

interface ControlFlowGraphMaker {
	ControlFlowGraph makeCfgOfMethod(Class<?> theClass, String methodName);
}
