package com.seideun.java.test_generator;

import java.util.List;

interface ConstraintSolver {
	List<ArgumentList> makeSatisfiableInput(List<Path> paths);
}
