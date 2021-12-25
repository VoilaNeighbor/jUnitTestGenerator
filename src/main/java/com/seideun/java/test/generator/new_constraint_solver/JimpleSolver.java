package com.seideun.java.test.generator.new_constraint_solver;

import com.microsoft.z3.*;
import com.seideun.java.test.generator.constriant_solver.TodoException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import soot.Unit;
import soot.Value;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JimpleLocal;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * A Jimple symbol solver to support symbolic execution on Jimple code.
 * <p>
 * This class is effectively a wrapper around Z3.
 * You can compare it as a special Z3 Solver + Model that recognizes Jimple
 * constraints. That is, it can solve Jimple constraints and find concrete
 * values of Jimple symbols.
 */
@SuppressWarnings("unchecked")
@AllArgsConstructor
public class JimpleSolver {
	// A conflict between z3 and beautiful code is that z3 requires you to call
	// mkXXX and remember the symbols. This forces you to introduce cache
	// collections.
	private final JimpleAwareZ3Context z3 = new JimpleAwareZ3Context();

	/**
	 * Todo(Seideun):
	 *   I see these 2 interfaces should reside in a higher level.
	 *   Extract them to another class.
	 *
	 * @param path Starting from the method entrance. Is a list to enforce
	 *             orderliness.
	 * @return Input symbols in order of encounter.
	 */
	public List<JimpleLocal> findInputSymbols(List<Unit> path) {
		// Input locals are defined by JIdentityStmt in Soot.
		return path.stream()
			.sequential()
			.filter(x -> x instanceof JIdentityStmt)
			.map(x -> ((JimpleLocal) ((JIdentityStmt) x).getLeftOp()))
			.toList();
	}

	public List<Object> solveSymbols(
		List<JimpleLocal> inputSymbols, List<Unit> path
	) {
		// Todo(Seideun): Finish here
		// Sometimes, the symbol solver is unable to soundly solve a set of
		// constraints, nor can it prove it unsatisfiable. In this case, we
		// assign an arbitrary value to the mysterious symbols, instead of
		// giving up the whole case.
		//
		// We check if there are some unsolved symbols by examining the
		// returned model to see if the symbol is still an Expr, not a concrete
		// value.
		var result = new ArrayList<>();
		for (var i: inputSymbols) {
			result.add(0);
		}
		return result;
	}

	public Pair<Object, Status> findConcreteValueOf(
		JimpleLocal symbol, Value relatedConstraints
	) {
		return findConcreteValueOf(symbol, singletonList(relatedConstraints));
	}

	/**
	 * Solve the constraints and find a valid concrete value for the input symbol.
	 *
	 * @param symbol             An input symbol with name and type.
	 * @param relatedConstraints Constraints related to that symbol.
	 * @return { concrete value or null, ... }
	 */
	public Pair<Object, Status> findConcreteValueOf(
		JimpleLocal symbol, List<Value> relatedConstraints
	) {
		var solver = z3.mkSolver();
		var status = solver.check(z3.add(relatedConstraints));
		if (status == Status.SATISFIABLE) {
			var value = findConcreteValueOf(z3.add(symbol), solver.getModel());
			return Pair.of(value, status);
		} else {
			return Pair.of(null, status);
		}
	}

	private static Object findConcreteValueOf(Expr z3Symbol, Model z3Model) {
		// More on the 2nd bool parameter in `z3Model.eval`:
		// https://z3prover.github.io/api/html/group__capi.html#gadb6ff55c26f5ef5607774514ee184957
		// Simply put, if the parameter is true, unbounded symbols will be assigned
		// some arbitrary values too. This simplifies our logic here.
		return switch (z3Model.eval(z3Symbol, true)) {
			case IntNum x -> x.getInt();
			case RatNum x -> toDouble(x);
			default -> throw new TodoException(z3Symbol);
		};
	}

	private static Object toDouble(RatNum x) {
		return (double) x.getNumerator().getInt() /
			(double) x.getDenominator().getInt();
	}
}
