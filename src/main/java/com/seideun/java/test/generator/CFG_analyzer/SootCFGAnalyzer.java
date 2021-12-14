package com.seideun.java.test.generator.CFG_analyzer;

import soot.Unit;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JReturnStmt;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.List;

public class SootCFGAnalyzer {

//    private UnitGraph ug; //zoot控制流图
//    private Body body; //一个函数里面的变量

	public static List<List<Unit>> findPrimePaths(UnitGraph ug) {
		List<List<Unit>> simplePathSet = new ArrayList<>();
		List<List<Unit>> extendPathSet = new ArrayList<>();

		//初始化simplePathSet = N(长度为0的集合)

		Chain<Unit> myUnit = ug.getBody().getUnits();
		for (Unit s: myUnit) {
			List<Unit> temp = new ArrayList<>();
			temp.add(s);
			simplePathSet.add(temp);
		}

		extendPathSet = new ArrayList<>(simplePathSet);
		while (!extendPathSet.isEmpty()) {
			List<Unit> p = extendPathSet.get(0);
			//判断p中是否有return语句
			boolean pHasReturn = false;
			for (Unit s: p) {
				if (s instanceof JReturnStmt) {
					extendPathSet.remove(0);
					pHasReturn = true;
					break;
				}
			}
			//p中如果没有return则需要扩展
			int num = p.size() - 1;
			Unit tail = p.get(num);
			if (!pHasReturn) {
				//找p扩展一个节点的所有路径
				List<Unit> a = ug.getSuccsOf(tail);
				List<Unit> succsUnitSet = new ArrayList<>(a);
				//System.out.println(succsUnitSet.size());
				for (Unit s: succsUnitSet) {
					//copy
					List<Unit> temp = new ArrayList<>(p);
					if (s instanceof JGotoStmt) {
						List<Unit> later = ug.getSuccsOf(s);
						if (later.size() == 1 && (later.get(0) instanceof JReturnStmt)) {
							temp.add(s);
							temp.add(later.get(0));
						} else {
							temp.add(s);
						}
					} else {
						temp.add(s);
					}
					//判断路径是不是简单路径
					boolean isSimplePath = true;
					for (int i = 0; i < temp.size(); i++) {
						for (int j = 0; j < temp.size(); j++) {
							if (temp.get(i) == temp.get(j)) {
								if (!((i == 0 && j == temp.size() - 1) || i == j)) {
									isSimplePath = false;
									break;
								}

							}
						}
					}
					//如果是简单路径把路径放入simplePathSet和extendPathSet
					if (isSimplePath) {
						extendPathSet.add(temp);
						simplePathSet.add(temp);
					}

				}

				extendPathSet.remove(0);

			}
		}
		List<List<Unit>> pathNeedRemove = new ArrayList<>();
		//对primePathSet去重
		for (int i = 0; i < simplePathSet.size(); i++) {
			List<Unit> patha = simplePathSet.get(i);
			for (int j = i + 1; j < simplePathSet.size(); j++) {
				if (i == j) {
					continue;
				}
				List<Unit> pathb = simplePathSet.get(j);
				if (pathb.size() > patha.size()) {
					if (hasSameArr(patha, pathb)) {
						boolean c = pathNeedRemove.contains(patha);
						if (!c) {
							pathNeedRemove.add(patha);
						}
					}
				} else {
					if (hasSameArr(pathb, patha)) {
						boolean c = pathNeedRemove.contains(pathb);
						if (!c) {
							pathNeedRemove.add(pathb);
						}
					}
				}
			}
		}
		for (List<Unit> p: pathNeedRemove) {
			simplePathSet.remove(p);
		}
		int i = 1;

		for (List<Unit> p: simplePathSet) {
			System.out.println("num: " + i + " one prime path is: ");
			for (Unit s: p) {
				System.out.println(s.toString());
			}
			i++;
			System.out.println(" ");
		}
		return simplePathSet;

	}

	public static boolean hasSameArr(List<Unit> patha, List<Unit> pathb) {

		for (int q = 0; q < pathb.size(); q++) {
			if (patha.get(0) == pathb.get(q)) {
				int k2 = 1;
				int q2 = q + 1;
				while (k2 < patha.size() && q2 < pathb.size()) {
					if (patha.get(k2) == pathb.get(q2)) {
						k2++;
						q2++;
					} else {
						break;
					}
				}
				if (k2 == patha.size()) {
					return true;
				}

			}

		}

		return false;
	}

	public static List<Path> findCompleteTest(
		List<List<Unit>> primePath,
		UnitGraph ug
	) {
		List<Path> completeTestPath = new ArrayList<>();

		//int i = 1;
		for (List<Unit> s: primePath) {
			Path temp = new Path(ug, s);
//            System.out.println("num: "+ i + " one complete path is:");
//            for(Unit m:temp.oneCompletePath){
//                System.out.println(m.toString());
//            }
//            System.out.println("num: "+ i + " path constrain is:");
//            System.out.println(temp.completePathConstraint);
//            System.out.println(" ");
//            i += 1;
			completeTestPath.add(temp);
		}
		//对completeTestPath去重
		completeTestPath = deleteDuplicatePath(completeTestPath);

		return completeTestPath;
	}

	public static List<Path> deleteDuplicatePath(List<Path> oldPathList) {
		List<Path> pathNeedRemove = new ArrayList<>();
		//对primePathSet去重
		for (int i = 0; i < oldPathList.size(); i++) {
			List<Unit> patha = oldPathList.get(i).oneCompletePath;
			for (int j = i + 1; j < oldPathList.size(); j++) {
				if (i == j) {
					continue;
				}
				List<Unit> pathb = oldPathList.get(j).oneCompletePath;
				if (pathb.size() > patha.size()) {
					if (hasSameArr(patha, pathb)) {
						boolean c = pathNeedRemove.contains(patha);
						if (!c) {
							pathNeedRemove.add(oldPathList.get(i));
						}
					}
				} else {
					if (hasSameArr(pathb, patha)) {
						boolean c = pathNeedRemove.contains(pathb);
						if (!c) {
							pathNeedRemove.add(oldPathList.get(j));
						}
					}
				}
			}
		}
		List<Path> newPathList = new ArrayList<>(oldPathList);
		for (Path p: pathNeedRemove) {
			newPathList.remove(p);
		}
		return newPathList;
	}

}
