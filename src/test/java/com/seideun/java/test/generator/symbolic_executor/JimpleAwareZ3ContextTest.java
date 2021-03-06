package com.seideun.java.test.generator.symbolic_executor;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import soot.ArrayType;
import soot.IntType;
import soot.jimple.IntConstant;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JLengthExpr;
import soot.jimple.internal.JimpleLocal;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class JimpleAwareZ3ContextTest {
	JimpleAwareZ3Context context = new JimpleAwareZ3Context();

	@Test
	void assignLengthStoresBothArrSymbolAndLenSymbol() {
		var array = new JimpleLocal("arr", ArrayType.v(IntType.v(), 1));
		var len = new JimpleLocal("len", IntType.v());
		var assign = new JAssignStmt(len, new JLengthExpr(array));

		context.addConstraint(assign);

		var symbols = context.symbolMap();
		assertNotNull(symbols.get(array));
		assertNotNull(symbols.get(len));
	}

	@Test
	void assignLengthMapsJArrToLenSymbol() {
		var array = new JimpleLocal("arr", ArrayType.v(IntType.v(), 1));
		var len = new JimpleLocal("len", IntType.v());
		var assign = new JAssignStmt(len, new JLengthExpr(array));

		context.addConstraint(assign);

		var symbols = context.symbolMap();
		var lenMap = context.lengthMap();
		assertEquals(lenMap.get(array), symbols.get(len));
	}

	@Test
	void hasBoundaryCheckWithConstantIndex() {
		var array = new JimpleLocal("arr", ArrayType.v(IntType.v(), 1));
		var len = new JimpleLocal("len", IntType.v());
		var ref = new JArrayRef(array, IntConstant.v(1));
		context.addConstraint(new JAssignStmt(len, new JLengthExpr(array)));
		var assignElem = new JAssignStmt(ref, IntConstant.v(2));

		var constraint = context.addConstraint(assignElem);

		System.out.println(constraint);
	}

	// with variable index?
}