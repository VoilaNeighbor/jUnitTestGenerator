package com.seideun.java.test_generator;

import soot.Unit;

import java.util.List;

public class IllFormedJIfStmt extends RuntimeException {
	public IllFormedJIfStmt(List<Unit> path) {
		super(path.toString());
	}
}
