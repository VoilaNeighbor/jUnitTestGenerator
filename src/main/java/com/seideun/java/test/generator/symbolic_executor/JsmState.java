package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import lombok.Data;
import soot.jimple.internal.JimpleLocal;

import java.util.Map;

/**
 * State of the Jimple Symbolic Machine (JSM). Result of its execution.
 */
public record JsmState(
	Context z3Context,
	Map<JimpleLocal, Expr> symbolTable
) {
}
