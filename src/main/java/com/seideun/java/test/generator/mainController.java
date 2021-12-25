package com.seideun.java.test.generator;

import com.seideun.java.test.generator.CFG_analyzer.Path;
import com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer;
import com.seideun.java.test.generator.CFG_generator.SootCFG;
import com.seideun.java.test.generator.examples.BasicExamples;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

import java.io.File;
import java.util.List;

public class mainController {

	public static void main(String[] ch) {
		String clspath = System.getProperty("user.dir") + File.separator +
			"target" +
			File.separator + "test-classes";
		String clsName = BasicExamples.class.getName();
		String methodName = "arrayTest";

		UnitGraph ug = SootCFG.getMethodCFG(clspath, clsName, methodName);

		//找到基础路径
		List<List<Unit>> primerPath = SootCFGAnalyzer.findPrimePaths(ug);

		//根据基础路径，扩展成完整路径(并且找到了constrains)，去掉了重复的完整路径
		//todo: 完整路径的contrains 有问题，没有找到满足可以进行n次循环的
		List<Path> completeTestPath
			= SootCFGAnalyzer.findCompleteTest(primerPath, ug);

		//输出看一下
		int i = 1;
		for (Path p: completeTestPath) {
			System.out.println("num : " + i + " one completePath: ");
			List<Unit> oneC = p.oneCompletePath;
			for (Unit c: oneC) {
				System.out.println(c.toString());
			}
			i += 1;
			System.out.println("constrains: " + p.completePathConstraint);
			System.out.println(" ");
		}

		//求解

	}

}
