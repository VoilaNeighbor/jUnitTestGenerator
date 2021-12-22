package com.seideun.java.test_generator;

import soot.toolkits.graph.UnitGraph;

import java.util.List;

interface PrimePathFinder {
	List<Path> findPrimePaths(UnitGraph controlFlowGraph);
}
