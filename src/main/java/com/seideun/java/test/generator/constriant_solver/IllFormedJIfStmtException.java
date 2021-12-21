package com.seideun.java.test.generator.constriant_solver;

import soot.Unit;

import java.util.List;

public class IllFormedJIfStmtException extends RuntimeException {
	public IllFormedJIfStmtException(List<Unit> path) {
		super(path.toString());
	}
}
