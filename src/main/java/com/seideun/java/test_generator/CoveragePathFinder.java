package com.seideun.java.test_generator;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.List;

public class CoveragePathFinder {
	public static List<List<Unit>> findCoveragePaths(UnitGraph controlFlowGraph) {
		List<List<Unit>> allPaths = new ArrayList<>();
		List<Unit> heads = controlFlowGraph.getHeads();
		assert heads.size() == 1 : "methods have only 1 entry point, I suppose?";
		for (Unit head: heads) {
			findCoveragePaths(head, new ArrayList<>(), controlFlowGraph, allPaths);
		}
		return allPaths;
	}

	/**
	 * @param thisUnit This unit under discourse. It is not in
	 *                 <code>before</code>.
	 *                 But it may be added to it just at the start of this method.
	 * @param thisPath takes ownership.
	 * @param graph    context of our search.
	 * @param result   container of all paths found.
	 */
	private static void findCoveragePaths(
		Unit thisUnit,
		List<Unit> thisPath,
		UnitGraph graph,
		List<List<Unit>> result
	) {
		thisPath.add(thisUnit);
		List<Unit> successors = graph.getSuccsOf(thisUnit);
		if (successors.isEmpty()) {
			result.add(thisPath);
		} else {
			for (Unit successor: successors) {
				findCoveragePaths(successor, new ArrayList<>(thisPath), graph, result);
			}
		}
	}
}
