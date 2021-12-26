package com.seideun.java.test.generator.new_constraint_solver;

import com.microsoft.z3.*;
import com.seideun.java.test.generator.constriant_solver.TodoException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import soot.Unit;
import soot.Value;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JimpleLocal;
import soot.util.Switchable;

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

	/**
	 * Traverse the path and extract constraints. Some new units might be added,
	 * so you might not be able to find them in the original path.
	 * <p>
	 * This is put here temporarily. I don't think the solver should take the
	 * responsibility of finding constraints.
	 */
	public List<Switchable> findConstraints(List<Unit> path) {
		var result = new ArrayList<Switchable>();
		for (int i = 0, iEnd = path.size(); i != iEnd; ++i) {
			var constraint = switch (path.get(i)) {
				case JIfStmt x -> {
					var c = x.getCondition();
					yield path.get(i + 1) == x.getTarget() ? new JInvertCondition(c) : c;
				}
				case JAssignStmt x -> x;
				default -> null;
			};
			if (constraint != null) {
				result.add(constraint);
			}
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
		JimpleLocal symbol, List<Switchable> relatedConstraints
	) {
		var result = findConcreteValueOf(singletonList(symbol), relatedConstraints);
		var symbolList = result.getLeft();
		return Pair.of(
			symbolList == null ? null : symbolList.get(0),
			result.getRight()
		);
	}

	// manually tested
	public Pair<List<Object>, Status> findConcreteValueOf(
		List<JimpleLocal> symbols, List<Switchable> relatedConstraints
	) {
		var solver = z3.mkSolver();
		var status = solver.check(z3.add(relatedConstraints));
		if (status == Status.SATISFIABLE) {
			var concreteValues = symbols.stream()
				.sequential()
				.map(s -> findConcreteValueOf(z3.getSymbol(s), solver.getModel()))
				.toList();
			return Pair.of(concreteValues, status);
		} else {
			return Pair.of(null, status);
		}
	}

	private Object findConcreteValueOf(Expr z3Symbol, Model z3Model) {
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
