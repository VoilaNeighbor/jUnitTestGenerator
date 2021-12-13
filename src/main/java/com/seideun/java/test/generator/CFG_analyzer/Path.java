package com.seideun.java.test.generator.CFG_analyzer;


import soot.Body;
import soot.Local;
import soot.Unit;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JReturnStmt;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

//测试路径集合
//包括基路径和完整路径
public class Path {

    public List<Unit> myPrimePath;
    public List<Unit> oneCompletePath;
    public String primePathConstraint;
    public String completePathConstraint;
    public List<String> valueList;
    public Object expectResult;

    public Path (UnitGraph ug,List<Unit> myPrimePath){
        this.primePathConstraint = calPathConstraint(myPrimePath,ug);
        this.myPrimePath = myPrimePath;
        this.oneCompletePath = findOneCompletePath(ug,myPrimePath);
        this.completePathConstraint = calPathConstraint(oneCompletePath,ug);
        findValueList(ug);
    }

    private List<Local> getJVars(Body body) {
        //Jimple自身增加的Locals，不是被测代码真正的变量
        ArrayList<Local> jimpleVars = new ArrayList<Local>();
        for (Local l : body.getLocals()) {
            if (l.toString().startsWith("$")) jimpleVars.add(l);
        }
        return jimpleVars;
    }

    //todo：找到路径上的constraint，有缺陷，还不支持循环
    public String calPathConstraint(List<Unit> path,UnitGraph ug) {

        List<Local> jVars = getJVars(ug.getBody());

        for(int i = 0;i < jVars.size();i++){
            System.out.println(jVars.get(i));
        }

        String pathConstraint = "";
        String expectedResult = "";

        HashMap<String, String> assignList = new HashMap<>();
        ArrayList<String> stepConditionsWithJimpleVars = new ArrayList<String>();
        ArrayList<String> stepConditions = new ArrayList<String>();

        for (Unit stmt : path) {

            if (stmt instanceof JAssignStmt) {
                assignList.put(((JAssignStmt) stmt).getLeftOp().toString(), ((JAssignStmt) stmt).getRightOp().toString());
                continue;
            }
            if (stmt instanceof JIfStmt) {

                String ifstms = ((JIfStmt) stmt).getCondition().toString();
                int nextUnitIndex = path.indexOf(stmt) + 1;
                Unit nextUnit = path.get(nextUnitIndex);

                //如果ifstmt的后继语句不是ifstmt中goto语句，说明ifstmt中的条件为假
                if (!((JIfStmt) stmt).getTarget().equals(nextUnit))
                    ifstms = "!( " + ifstms + " )";
                else
                    ifstms = "( " + ifstms + " )";
                stepConditionsWithJimpleVars.add(ifstms);
                continue;
            }
            if (stmt instanceof JReturnStmt) {
                expectedResult = stmt.toString().replace("return", "").trim();
                this.expectResult =expectedResult;
            }
        }
        //System.out.println("The step conditions with JimpleVars are: " + stepConditionsWithJimpleVars);

        //bug 没有考虑jVars为空的情况
        if (jVars.size() != 0) {
            for (String cond : stepConditionsWithJimpleVars) {
                //替换条件里的Jimple变量
                for (Local lv : jVars) {
                    if (cond.contains(lv.toString())) {
                        stepConditions.add(cond.replace(lv.toString(), assignList.get(lv.toString()).trim()));
                    }
                }
            }
        } else
            stepConditions = stepConditionsWithJimpleVars;

        if (stepConditions.isEmpty())
            return "";
        pathConstraint = stepConditions.get(0);
        int i = 1;
        while (i < stepConditions.size()) {
            pathConstraint = pathConstraint + " && " + stepConditions.get(i);
            i++;
        }
        //System.out.println("The path expression is: " + pathConstraint);

        return pathConstraint;
    }

    public List<Unit> findOneCompletePath(UnitGraph ug,List<Unit>myPrimePath){
        List<Unit> completePath = new ArrayList<>(myPrimePath);
        List<Unit> forwardPath = new ArrayList<>();
        List<Unit> nextPath = new ArrayList<>();
        Unit primeStart = myPrimePath.get(0);
        Unit primeEnd = myPrimePath.get(myPrimePath.size() - 1);


        Unit begin = ug.getBody().getUnits().getFirst();
        List<Unit> end = ug.getTails();
        //找到前驱
        forwardPath = findForwardPath(begin,primeStart,ug,forwardPath);
        if(forwardPath.size()> 0 )forwardPath.remove(0);

        //找到后继
        nextPath = findNextPath(end,primeEnd,ug,nextPath);
        if(nextPath.size()> 0)nextPath.remove(0);

        //组合
        if(nextPath != null)completePath.addAll(nextPath);

        List<Unit> temp = new ArrayList<>();
        for(int i = forwardPath.size()-1;i >= 0;i--){
            Unit s = forwardPath.get(i);
            temp.add(s);
        }
        temp.addAll(completePath);
        return temp;
    }

    //找到基础路径的前面路径
    public List<Unit> findForwardPath(Unit begin,Unit nowStart,UnitGraph ug,List<Unit> forwardPath){
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
        while( !needToFind.isEmpty() && !isFind ){
            List<Unit> tmp = needToFind.get(0);
            needToFind.remove(0);

            Unit visit = tmp.get(tmp.size() - 1);
            List<Unit> needVisit = ug.getPredsOf(visit);
            for(Unit m:needVisit){
                List<Unit> tmp2 = new ArrayList<>(tmp);
                tmp2.add(m);
                if(m == begin){
                    isFind = true;
                    forwardPath = tmp2;
                }else{
                    needToFind.add(tmp2);
                }
            }

        }
        return forwardPath;
    }

    //找到基路径的后面路径
    public List<Unit> findNextPath(List<Unit> end,Unit nowEnd,UnitGraph ug,List<Unit> endPath){
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
        while( !needToFind.isEmpty() && !isFind ){
            List<Unit> tmp = needToFind.get(0);
            needToFind.remove(0);

            Unit visit = tmp.get(tmp.size() - 1);
            List<Unit> needVisit = ug.getSuccsOf(visit);
            for(Unit m:needVisit){
                List<Unit> tmp2 = new ArrayList<>(tmp);
                tmp2.add(m);
                if(m instanceof JReturnStmt){
                    isFind = true;
                    endPath = tmp2;
                }else{
                    needToFind.add(tmp2);
                }
            }

        }
        return endPath;
    }

    //找到路径中的赋值语句
    public void findValueList(UnitGraph ug){

        valueList = new ArrayList<>();

        Unit end = oneCompletePath.get(oneCompletePath.size()-1);
        String endValue = end.toString().replace("return", "").trim();
        for(Unit s:oneCompletePath){
            if(s instanceof JAssignStmt){
                String temp1 =s.toString();
                Object temp = ((JAssignStmt) s).leftBox.getValue();
                Object temp2 = ((JAssignStmt) s).rightBox.getValue();
               // System.out.println(temp2);
                valueList.add(temp1);
            }
        }

    }
}
