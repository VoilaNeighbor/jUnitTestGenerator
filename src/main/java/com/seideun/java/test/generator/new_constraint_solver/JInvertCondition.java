package com.seideun.java.test.generator.new_constraint_solver;

import lombok.SneakyThrows;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.internal.AbstractJimpleIntBinopExpr;
import soot.util.Switch;

import static java.lang.String.format;

/**
 * Just a wrapper for simplicity of translation to Z3 Expressions -- Jimple
 * has no notion of negation of bool expressions.
 */
public class JInvertCondition extends AbstractJimpleIntBinopExpr {
	public AbstractJimpleIntBinopExpr base;

	public JInvertCondition(Value base) {
		super(
			((AbstractJimpleIntBinopExpr) base).getOp1(),
			((AbstractJimpleIntBinopExpr) base).getOp2()
		);
		this.base = (AbstractJimpleIntBinopExpr) base;
	}

	@Override
	protected Unit makeBafInst(Type opType) {
		return null;
	}

	@SneakyThrows
	@Override
	protected String getSymbol() {
		var m = base.getClass().getMethod("getSymbol");
		m.setAccessible(true);
		return format(" !(%s) ", m.invoke(base));
	}

	@Override
	public Object clone() {
		return null;
	}

	@Override
	public void apply(Switch sw) {
	}
}
