package com.seideun.java.test_generator;

import soot.toolkits.graph.UnitGraph;

interface ControlFlowGraphMaker {
	UnitGraph makeControlFlowGraph(Class<?> theClass, String methodName);
}
