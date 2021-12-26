package com.seideun.java.test.generator.symbolic_executor;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import soot.jimple.internal.JimpleLocal;

import java.util.Map;

/**
 * A path the Jimple Symbolic-execution Machine takes.
 */
public record JsmPath(
	Context z3Context,
	Map<JimpleLocal, Expr> symbolTable
) {
}
