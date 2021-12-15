package com.seideun.java.test.generator.CFG_analyzer;


import soot.Body;
import soot.Local;
import soot.Unit;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JReturnStmt;
import soot.toolkits.graph.UnitGraph;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//测试路径集合
//包括基路径和完整路径
public class Path {

	public List<Unit> myPrimePath;
	public List<Unit> oneCompletePath;
	public String primePathConstraint;
	public String completePathConstraint;
	public List<String> valueList;
	public Object expectResult;
	public HashMap<String, LinkedHashMap<String, String>> assignMap;

	public Path(UnitGraph ug, List<Unit> myPrimePath) {
		this.primePathConstraint = calPathConstraint(myPrimePath, ug);
		this.myPrimePath = myPrimePath;
		this.oneCompletePath = findOneCompletePath(ug, myPrimePath);
		this.completePathConstraint = calPathConstraint(oneCompletePath, ug);
		findValueList(ug);
	}

	//todo：找到路径上的constraint
	public String calPathConstraint(List<Unit> path, UnitGraph ug) {
		List<Local> jVars = getJVars(ug.getBody());

		String pathConstraint = "";
		String expectedResult = "";

		assignMap = new HashMap<>();
		ArrayList<String> stepConditionsWithJimpleVars = new ArrayList<String>();
		ArrayList<String> stepConditions = new ArrayList<String>();

		for (int i = 0; i < jVars.size(); i++) {
			System.out.println(jVars.get(i));
		}
		for (Unit stmt: path) {
			if (stmt instanceof JAssignStmt) {
				String key = ((JAssignStmt) stmt).getLeftOp().toString();
				//查询是否存在该值
				LinkedHashMap<String, String> stateSet
					= assignMap.computeIfAbsent(key, k -> new LinkedHashMap<>());
				//如果不存在该值,新建

				String value = ((JAssignStmt) stmt).getRightOp().toString();
				//todo:对负数加括号 ,正则表达式不对
				Pattern pattern = Pattern.compile("-?[1-9]\\d*|0");//"^[-\\+]?([0-9]+\\
				// .?)?[0-9]+$"
				//Pattern pattern = Pattern.compile("^[-\\\\+]?([0-9]+\\\\.?)?[0-9]+$");
				Matcher isNum = pattern.matcher(value);
				if (!isNum.matches()) {
					int num = isNum.groupCount();
					while (num > 0) {
						String getStr = isNum.group(num);
						value = value.replaceFirst(value, "(" + getStr + ")");
						num -= 1;
					}
				}
				Set<String> keySet = assignMap.keySet();
				for (String tmp: keySet) {
					if (value.contains(tmp)) {
						Entry<String, String> entry = getTail(assignMap.get(tmp));
						if (entry != null) {
							String nowStateValue = entry.getValue();
							value = value.replace(tmp, "(" + nowStateValue + ")");
						}
					}
				}
				assignMap.get(key);
				LinkedHashMap<String, String> stateSet2 = assignMap.get(key);
				int num = stateSet2.size();
				String newKey = key + String.valueOf(num);
				stateSet2.put(newKey, value);
				assignMap.put(key, stateSet);
				continue;
			}
			if (stmt instanceof JIfStmt) {

				String ifstms = ((JIfStmt) stmt).getCondition().toString();
				Set<String> keys = assignMap.keySet();
				for (String tmp2: keys) {
					if (ifstms.contains(tmp2)) {
						String newValue = getTail(assignMap.get(tmp2)).getValue();
						ifstms = ifstms.replace(tmp2, "(" + newValue + ")");
					}
				}
				int nextUnitIndex = path.indexOf(stmt) + 1;
				Unit nextUnit = path.get(nextUnitIndex);

				//如果ifstmt的后继语句不是ifstmt中goto语句，说明ifstmt中的条件为假
				if (!((JIfStmt) stmt).getTarget().equals(nextUnit)) {
					ifstms = "!( " + ifstms + " )";
				} else {
					ifstms = "( " + ifstms + " )";
				}
				stepConditionsWithJimpleVars.add(ifstms);
				continue;
			}
			if (stmt instanceof JReturnStmt) {
				expectedResult = stmt.toString().replace("return", "").trim();
				this.expectResult = expectedResult;
			}
		}

		if (jVars.size() != 0) {
			stepConditions = stepConditionsWithJimpleVars;
		} else {
			stepConditions = stepConditionsWithJimpleVars;
		}

		if (stepConditions.isEmpty()) {
			return "";
		}
		pathConstraint = stepConditions.get(0);
		int i = 1;
		while (i < stepConditions.size()) {
			pathConstraint = pathConstraint + " && " + stepConditions.get(i);
			i++;
		}
		return pathConstraint;
	}

	public List<Unit> findOneCompletePath(UnitGraph ug, List<Unit> myPrimePath) {
		List<Unit> completePath = new ArrayList<>(myPrimePath);
		List<Unit> forwardPath = new ArrayList<>();
		List<Unit> nextPath = new ArrayList<>();
		Unit primeStart = myPrimePath.get(0);
		Unit primeEnd = myPrimePath.get(myPrimePath.size() - 1);


		Unit begin = ug.getBody().getUnits().getFirst();
		List<Unit> end = ug.getTails();
		//找到前驱
		forwardPath = findForwardPath(begin, primeStart, ug, forwardPath);
		if (forwardPath.size() > 0) {
			forwardPath.remove(0);
		}

		//找到后继
		nextPath = findNextPath(end, primeEnd, ug, nextPath);
		if (nextPath.size() > 0) {
			nextPath.remove(0);
		}

		//组合
		if (nextPath != null) {
			completePath.addAll(nextPath);
		}

		List<Unit> temp = new ArrayList<>();
		for (int i = forwardPath.size() - 1; i >= 0; i--) {
			Unit s = forwardPath.get(i);
			temp.add(s);
		}
		temp.addAll(completePath);
		return temp;
	}

	//找到路径中的赋值语句
	public void findValueList(UnitGraph ug) {

		valueList = new ArrayList<>();

		Unit end = oneCompletePath.get(oneCompletePath.size() - 1);
		String endValue = end.toString().replace("return", "").trim();
		for (Unit s: oneCompletePath) {
			if (s instanceof JAssignStmt) {
				String temp1 = s.toString();
				Object temp = ((JAssignStmt) s).leftBox.getValue();
				Object temp2 = ((JAssignStmt) s).rightBox.getValue();
				// System.out.println(temp2);
				valueList.add(temp1);
			}
		}

		LinkedHashMap<String, String> endSet = assignMap.get(endValue);
		Entry<String, String> lastValue = getTail(endSet);
		expectResult = lastValue.getValue();


	}

	private List<Local> getJVars(Body body) {
		//Jimple自身增加的Locals，不是被测代码真正的变量
		ArrayList<Local> jimpleVars = new ArrayList<Local>();
		for (Local l: body.getLocals()) {
			if (l.toString().startsWith("$")) {
				jimpleVars.add(l);
			}
		}
		return jimpleVars;
	}

	public <K, V> Entry<K, V> getTail(LinkedHashMap<K, V> map) {
		Iterator<Entry<K, V>> iterator = map.entrySet().iterator();
		Entry<K, V> tail = null;
		while (iterator.hasNext()) {
			tail = iterator.next();
		}
		return tail;
	}

	//找到基础路径的前面路径
	public List<Unit> findForwardPath(
		Unit begin,
		Unit nowStart,
		UnitGraph ug,
		List<Unit> forwardPath
	) {
//        if(begin == nowStart){
//            return forwardPath;
//        }
//        else {
//            List<Unit> predSet = ug.getPredsOf(nowStart);
//            for(Unit s:predSet){
//                List<Unit> temp = new ArrayList<>(forwardPath);
//                temp.add(s);
//                return findForwardPath(begin,s,ug,temp);
//            }
//        }
		List<List<Unit>> needToFind = new ArrayList<>();
		List<Unit> tempPath = new ArrayList<>();
		tempPath.add(nowStart);
		needToFind.add(tempPath);
		boolean isFind = false;
		while (!needToFind.isEmpty() && !isFind) {
			List<Unit> tmp = needToFind.get(0);
			needToFind.remove(0);

			Unit visit = tmp.get(tmp.size() - 1);
			List<Unit> needVisit = ug.getPredsOf(visit);
			for (Unit m: needVisit) {
				List<Unit> tmp2 = new ArrayList<>(tmp);
				tmp2.add(m);
				if (m == begin) {
					isFind = true;
					forwardPath = tmp2;
				} else {
					needToFind.add(tmp2);
				}
			}

		}
		return forwardPath;
	}

	//找到基路径的后面路径
	public List<Unit> findNextPath(
		List<Unit> end,
		Unit nowEnd,
		UnitGraph ug,
		List<Unit> endPath
	) {
//        if(end.contains(nowEnd)){
//            return endPath;
//        }
//        else {
//            List<Unit> endSet = ug.getSuccsOf(nowEnd);
//            if(endSet == null) return null;
//            for(Unit s:endSet){
//                List<Unit> temp = new ArrayList<>(endPath);
//                temp.add(s);
//                return findNextPath(end,s,ug,temp);
//            }
//        }
//        return null;
		List<List<Unit>> needToFind = new ArrayList<>();
		List<Unit> tempPath = new ArrayList<>();
		tempPath.add(nowEnd);
		needToFind.add(tempPath);
		boolean isFind = false;
		while (!needToFind.isEmpty() && !isFind) {
			List<Unit> tmp = needToFind.get(0);
			needToFind.remove(0);

			Unit visit = tmp.get(tmp.size() - 1);
			List<Unit> needVisit = ug.getSuccsOf(visit);
			for (Unit m: needVisit) {
				List<Unit> tmp2 = new ArrayList<>(tmp);
				tmp2.add(m);
				if (m instanceof JReturnStmt) {
					isFind = true;
					endPath = tmp2;
				} else {
					needToFind.add(tmp2);
				}
			}

		}
		return endPath;
	}
}
