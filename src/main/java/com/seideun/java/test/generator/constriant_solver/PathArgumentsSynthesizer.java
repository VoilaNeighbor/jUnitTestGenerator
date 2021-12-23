package com.seideun.java.test.generator.constriant_solver;

import com.microsoft.z3.*;
import com.microsoft.z3.Context;
import soot.*;
import soot.jimple.DoubleConstant;
import soot.jimple.IntConstant;
import soot.jimple.ParameterRef;
import soot.jimple.RealConstant;
import soot.jimple.internal.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Reads a path and synthesize arguments that satisfy the constraints along it.
 * <p>
 * It can synthesize <em>ONE</em> path only. Use multiple instances of this
 * if you want to solve many paths.
 */
@SuppressWarnings("unchecked")
public class PathArgumentsSynthesizer {
	protected Context z3Context;
	private Map<JimpleLocal, Integer> timesLocalsAssigned;
	private List<BoolExpr> constraints;
	private List<Expr<?>> methodArgs;
	private HashMap<Expr<?>,Value> listValue;
	private List<Expr<?>> listArgs;
	private HashMap<Value,Expr<?>> listLengthTable;
	private HashMap<Value,HashMap<Integer,Expr<?>>> listMember;

	public PathArgumentsSynthesizer() {
		clear();
	}

	public void clear() {
		z3Context = new Context();
		timesLocalsAssigned = new HashMap<>();
		constraints = new ArrayList<>();
		listValue = new HashMap<>();
		listLengthTable = new HashMap<>();
		listMember = new HashMap<>();
		listArgs = new ArrayList<>();
	}

	public static List<List<Object>> synthesize(Collection<List<Unit>> paths) {
		return paths.stream()
			.map(path -> {
				PathArgumentsSynthesizer x = new PathArgumentsSynthesizer();
				x.store(path);
				return x.synthesizeArguments().get();
			}).collect(Collectors.toList());
	}

	private Object makeRandom(Expr<?> variable) {
		if (variable instanceof IntExpr) {
			return 0;
		} else if (variable instanceof RealExpr) {
			return 0.0;
		} else if(variable instanceof ArrayExpr){
//			if(variable.getSort() == z3Context.mkCharSort())
//			return "aaa";
//			else if(variable.getSort() == z3Context.mkIntSort()){
//				return new int[]{1,2,4};
//			}
			return new int[]{1,2,4};
		}else {
			throw new TodoException(variable);
		}

	}

	/**
	 * Store the parameters and constraints along the path.
	 * <p>
	 * There are occasions when a complete path is consisted of multiple parts.
	 * So the class supports storing them incrementally. This class will try to
	 * solve all constraints on the paths together when you call
	 * {@link #synthesizeArguments() synthesizeArguments}.
	 */
	public void store(List<Unit> path) {
		// JIfStmts and JGotoStmts are guaranteed to have nodes following them.
		if (path.get(path.size() - 1) instanceof JIfStmt) {
			throw new IllFormedJIfStmtException(path);
		}
		collectArgs(path);
		collectConstraints(path);
	}

	public List<Expr<?>> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	public Optional<List<Object>> synthesizeArguments() {
		Solver solver = z3Context.mkSolver();
		solver.add(constraints.toArray(new BoolExpr[0]));
		if (solver.check() != Status.SATISFIABLE) {
			return Optional.empty();
		}
		Model model = solver.getModel();
		List<Object> result = new ArrayList<>();

		for (Expr<?> arg: methodArgs) {
			Expr<?> value = model.eval(arg, false);
			if(listValue.get(arg)!=null){
				int[] a;
				Value name = listValue.get(arg);
                if(listLengthTable.containsKey(name)){
					IntExpr lenValue = (IntExpr) model.eval(listLengthTable.get(name),false);
					int len = ((IntNum) (lenValue)).getInt();
					a = new int[len];
					HashMap<Integer,Expr<?>> listExp = listMember.get(name);
					Set<Integer> keySet =listExp.keySet();
					for(Integer index:keySet){
						Expr<?> temp = listExp.get(index);
						temp = model.eval(temp,false);
						a[index] = ((IntNum) (temp)).getInt();
					}
					result.add(a);continue;
				}else{
					if(listMember.containsKey(name)){
						HashMap<Integer,Expr<?>> listExp = listMember.get(name);
						Set<Integer> keySet =listExp.keySet();
						int len = Collections.max(keySet);
						a = new int[len+1];
						for(Integer index:keySet){
							Expr<?> temp = listExp.get(index);
							temp = model.eval(temp,false);
							a[index] = ((IntNum) (temp)).getInt();
						}
						result.add(a);continue;
					}else{
						result.add(makeRandom(value));
					}
				}

			}
			if(listArgs.contains(arg))continue;
			if (value instanceof IntNum) {
				result.add(((IntNum) value).getInt());
			} else if (value instanceof RatNum) {
				RatNum ratNum = (RatNum) value;
				result.add((double) ratNum.getNumerator().getInt64() /
					(double) ratNum.getDenominator().getInt64());
			} else {
				result.add(makeRandom(value));
			}
		}
		return Optional.of(result);
	}

	private void collectArgs(List<Unit> path) {
		List<Expr<?>> list = new ArrayList<>();
		List<Value> values = new ArrayList<>();
		for (Unit unit : path) {
			if (unit instanceof JIdentityStmt || unit instanceof JAssignStmt) {
				if(unit instanceof JAssignStmt){
					JAssignStmt x = (JAssignStmt) unit;
					Value temp2 = x.getLeftOp();
                    if(!values.contains(temp2)){
						if (x.getRightOp() instanceof ParameterRef) {
							Value name = x.getLeftOp();
							Expr<?> expr = convertJValueToZ3Expr(name);
							list.add(expr);
							values.add(name);
							if(x.getRightOp().getType() instanceof ArrayType){
								if(listValue.get(name)==null){
									listValue.put(expr,x.getLeftOp());
									listArgs.add(expr);
								}

							}
						}else if(x.getRightOp() instanceof JLengthExpr){
							Value arraylistName = ((JLengthExpr) x.getRightOp()).getOpBox().getValue();
							Expr<?> lengthExpr = convertJValueToZ3Expr(x.getLeftOp());
							if(!listLengthTable.containsKey(arraylistName)){
								listLengthTable.put(arraylistName,lengthExpr);
								listArgs.add(lengthExpr);
							}
						}else if(x.getRightOp() instanceof JArrayRef){
							Value arraylistName = ((JArrayRef) x.getRightOp()).getBase();
							Expr<?> listMemberExpr = convertJValueToZ3Expr(x.getLeftOp());
							int num = ((IntConstant)(((JArrayRef) x.getRightOp()).getIndex())).hashCode();
							if(listMember.containsKey(arraylistName)){
								HashMap<Integer,Expr<?>> temp = listMember.get(arraylistName);
								temp.put(num,listMemberExpr);
								listArgs.add(listMemberExpr);
							}else{
								HashMap<Integer,Expr<?>> temp = new HashMap<>();
								temp.put(num,listMemberExpr);
								listMember.put(arraylistName,temp);
								listArgs.add(listMemberExpr);
							}
						}
					}
					continue;
				}
				//
				JIdentityStmt x = (JIdentityStmt) unit;
				if (x.getRightOp() instanceof ParameterRef) {
					Value name = x.getLeftOp();
					Expr<?> expr = convertJValueToZ3Expr(name);
					list.add(expr);
					values.add(name);
					if(x.getRightOp().getType() instanceof ArrayType){
						if(listValue.get(x.getLeftOp())==null){
							listValue.put(expr,x.getLeftOp());
							listArgs.add(expr);
						}

					}
				}else if(x.getRightOp() instanceof JLengthExpr){
					Value arraylistName = ((JLengthExpr) x.getRightOp()).getOpBox().getValue();
					Expr<?> lengthExpr = convertJValueToZ3Expr(x.getLeftOp());
					if(!listLengthTable.containsKey(arraylistName)){
						listLengthTable.put(arraylistName,lengthExpr);
						listArgs.add(lengthExpr);
					}
				}else if(x.getRightOp() instanceof JArrayRef){
					Value arraylistName = ((JLengthExpr) x.getRightOp()).getOpBox().getValue();
					Expr<?> listMemberExpr = convertJValueToZ3Expr(x.getLeftOp());
					int num = ((IntConstant)(((JArrayRef) x.getRightOp()).getIndex())).hashCode();
					if(listMember.containsKey(arraylistName)){
						HashMap<Integer,Expr<?>> temp = listMember.get(arraylistName);
						temp.put(num,listMemberExpr);
						listArgs.add(listMemberExpr);
					}else{
						HashMap<Integer,Expr<?>> temp = new HashMap<>();
						temp.put(num,listMemberExpr);
						listMember.put(arraylistName,temp);
						listArgs.add(listMemberExpr);
					}
				}
			}
		}
		methodArgs = list;
	}

	private void collectConstraints(List<Unit> path) {
		for (int i = 0, end = path.size() - 1; i != end; ++i) {
			Unit thisUnit = path.get(i);
			if (thisUnit instanceof JAssignStmt) {
				incrementTimesAssigned(((JAssignStmt) thisUnit).getLeftOp());

				//TODO:添加等式条件
			} else if (thisUnit instanceof JIfStmt) {
				constraints.add(extractConstraintOf((JIfStmt) thisUnit, i, path));
			}
		}
	}

	private void incrementTimesAssigned(Value local) {
		timesLocalsAssigned.compute(
			(JimpleLocal) local,
			(k, v) -> 1 + (v == null ? 0 : v)
		);
	}

	private BoolExpr extractConstraintOf(
		JIfStmt theIf, int idxInPath, List<Unit> path
	) {
		BoolExpr constraint =
			(BoolExpr) convertJValueToZ3Expr(theIf.getCondition());
		return theIf.getTarget() != path.get(idxInPath + 1)
			? z3Context.mkNot(constraint)
			: constraint;
	}

	private Expr<?> convertJValueToZ3Expr(Value jValue) {
		if (jValue instanceof JimpleLocal) {
			JimpleLocal jimpleLocal = ((JimpleLocal) jValue);
			Integer timesReassigned = timesLocalsAssigned.get(jimpleLocal);
			String varName = jimpleLocal.getName() +
				(timesReassigned == null ? "" : ("$" + timesReassigned));
			if (jimpleLocal.getType() == IntType.v()) {
				return z3Context.mkIntConst(varName);
			} else if (jimpleLocal.getType() == DoubleType.v()) {
				return z3Context.mkRealConst(varName);
			} else if(jimpleLocal.getType() == ArrayType.v(IntType.v(),1)){
			   return z3Context.mkArrayConst(varName, z3Context.mkIntSort(), z3Context.mkIntSort());
			}else if(jimpleLocal.getType() == RefType.v("java.lang.String")){
				//TODO:字符串相关
				return z3Context.mkArrayConst(varName, z3Context.mkIntSort(), z3Context.mkCharSort());
			}
			throw new TodoException(jimpleLocal.getType());
		} else if (jValue instanceof IntConstant) {
			return z3Context.mkInt(((IntConstant) jValue).value);
		} else if (jValue instanceof RealConstant) {
			Rational rational = new Rational(((DoubleConstant) jValue).value);
			return z3Context.mkReal(rational.numerator, rational.denominator);
		} else if (jValue instanceof JGeExpr) {
			JGeExpr jGeExpr = (JGeExpr) jValue;
			return z3Context.mkGe(
				(Expr<? extends ArithSort>) convertJValueToZ3Expr(
					jGeExpr.getOp1()
				),
				(Expr<? extends ArithSort>) convertJValueToZ3Expr(
					jGeExpr.getOp2()
				)
			);
		} else if (jValue instanceof JLtExpr) {
			JLtExpr jGeExpr = (JLtExpr) jValue;
			return z3Context.mkLt(
				(Expr<? extends ArithSort>) convertJValueToZ3Expr(
					jGeExpr.getOp1()
				),
				(Expr<? extends ArithSort>) convertJValueToZ3Expr(
					jGeExpr.getOp2()
				)
			);
		} else if (jValue instanceof JLeExpr) {
			JLeExpr jGeExpr = (JLeExpr) jValue;
			return z3Context.mkLt(
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp1()
					),
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp2()
					)
			);
		} else if (jValue instanceof JGtExpr) {
			JGtExpr jGeExpr = (JGtExpr) jValue;
			return z3Context.mkLt(
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp1()
					),
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp2()
					)
			);
		} else if (jValue instanceof JAndExpr) {
			JAndExpr jGeExpr = (JAndExpr) jValue;
			return z3Context.mkLt(
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp1()
					),
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp2()
					)
			);
		} else if (jValue instanceof JOrExpr) {
			JOrExpr jGeExpr = (JOrExpr) jValue;
			return z3Context.mkLt(
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp1()
					),
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp2()
					)
			);
		}else if (jValue instanceof JShlExpr) {
			JShlExpr jGeExpr = (JShlExpr) jValue;
			return z3Context.mkLt(
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp1()
					),
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp2()
					)
			);
		}else if (jValue instanceof JShrExpr) {
			JShrExpr jGeExpr = (JShrExpr) jValue;
			return z3Context.mkLt(
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp1()
					),
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp2()
					)
			);
		}else if (jValue instanceof JXorExpr) {
			JXorExpr jGeExpr = (JXorExpr) jValue;
			return z3Context.mkLt(
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp1()
					),
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp2()
					)
			);
		}else if (jValue instanceof JUshrExpr) {
			JUshrExpr jGeExpr = (JUshrExpr) jValue;
			return z3Context.mkLt(
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp1()
					),
					(Expr<? extends ArithSort>) convertJValueToZ3Expr(
							jGeExpr.getOp2()
					)
			);
		}else {
			throw new TodoException();
		}
	}
}
