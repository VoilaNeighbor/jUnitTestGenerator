package com.seideun.java.test.generator.smt;

import java.util.Collection;

/**
 * The method under test is not recorded here. That information should be
 * tracked at the call site.
 */
public record TestCase(Collection<Object> arguments, Object expectedResult) {
}