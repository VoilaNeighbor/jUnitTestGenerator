package com.seideun.java.test_generator;

import soot.toolkits.graph.UnitGraph;

interface ControlFlowGraphMaker {
	UnitGraph makeCfgOfMethod(Class<?> theClass, String methodName);
}
