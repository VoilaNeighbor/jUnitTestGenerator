package com.seideun.java.test.generator.new_constraint_solver;

import org.junit.jupiter.api.Test;
import soot.ArrayType;
import soot.IntType;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JLengthExpr;
import soot.jimple.internal.JimpleLocal;

import static org.junit.jupiter.api.Assertions.*;

class JimpleAwareZ3ContextTest {
	JimpleAwareZ3Context context = new JimpleAwareZ3Context();

	@Test
	void assignStmtBuildsIndexFromArrayToLength() {
		var array = new JimpleLocal("arr", ArrayType.v(IntType.v(), 1));
		var len = new JimpleLocal("len", IntType.v());
		var assign = new JAssignStmt(len, new JLengthExpr(array));

		context.add(assign);

		var symbols = context.symbolMap();
		var lenMap = context.lengthMap();
		assertNotNull(lenMap.get(array));
		assertEquals(lenMap.get(array), symbols.get(len));
	}

	@Test
	void boundaryChecksInArrayIndexing() {

	}
}