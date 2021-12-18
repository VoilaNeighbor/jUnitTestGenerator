package com.seideun.java.test_generator;

import soot.Unit;

import java.util.List;

public class IllFormedJIfStmtException extends RuntimeException {
	public IllFormedJIfStmtException(List<Unit> path) {
		super(path.toString());
	}
}
