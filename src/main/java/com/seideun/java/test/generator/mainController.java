package com.seideun.java.test.generator;

import com.seideun.java.test.generator.CFG_analyzer.Path;
import com.seideun.java.test.generator.CFG_analyzer.SimpleVeryBusyExpressions;
import com.seideun.java.test.generator.CFG_analyzer.SootCFGAnalyzer;
import com.seideun.java.test.generator.CFG_analyzer.VeryBusyExpressions;
import com.seideun.java.test.generator.CFG_generator.SootCFG;
import com.seideun.java.test.generator.constriant_solver.TestCaseInputBuilder;
import com.seideun.java.test.generator.constriant_solver.Z3Solver;
import com.seideun.java.test.generator.java_reader.JavaReader;
import soot.*;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class mainController {

    public static void main(String []ch){
        String clspath = System.getProperty("user.dir") + File.separator + "target" + File.separator + "test-classes";
        String clsName = "cut.LogicStructure";
        String methodName = "mywhile";

        UnitGraph ug = SootCFG.getMethodCFG(clspath,clsName,methodName);
        Body body = SootCFG.getMethodBody(clspath,clsName,methodName);
        //找到基础路径
        List<List<Unit> > primerPath = SootCFGAnalyzer.findPrimePaths(ug);
        //根据基础路径，扩展成完整路径(并且找到了constrains)，去掉了重复的完整路径
        //todo: 完整路径的contrains 有问题，没有找到满足可以进行n次循环的
        List<Path> completeTestPath = SootCFGAnalyzer.findCompleteTest(primerPath,ug);

        //输出看一下
        int i = 1;
        for(Path p:completeTestPath){
            System.out.println("num : "+i+" one completePath: ");
            List<Unit> oneC = p.oneCompletePath;
            for(Unit c:oneC){
                System.out.println(c.toString());
            }
            i+=1;
            System.out.println("constrains: "+p.completePathConstraint);
            System.out.println(" ");
        }
 //Build the CFG and run the analysis
//        UnitGraph g = new ExceptionalUnitGraph(body);


//        VeryBusyExpressions an = new SimpleVeryBusyExpressions(g);
//        Iterator i  = g.iterator();
//        int q = 1;
//        while(i.hasNext()){
//            Unit u  = (Unit)i.next();
//            List IN = an.getBusyExpressionsBefore(u);
//            List OUT = an.getBusyExpressionsAfter(u);
//            System.out.println("num: "+q);
//            System.out.println("u:"+u);
//            System.out.println("out:"+OUT);
//            System.out.println("In:"+IN);
//            System.out.println(" ");
//            q +=1;
//        }

        //求解
        List<Object> testValue = new ArrayList<>();
        for(Path s:completeTestPath){
            try {
                System.out.println("ans:"+Z3Solver.solve(s.completePathConstraint, testValue )+ " expect: "+s.expectResult+" valueList: "+s.valueList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }



}
