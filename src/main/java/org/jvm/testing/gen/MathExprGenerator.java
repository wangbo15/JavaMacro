package org.jvm.testing.gen;

//import com.github.javaparser.ast.body.VariableDeclarator;
//import com.github.javaparser.ast.expr.VariableDeclarationExpr;
//import com.github.javaparser.ast.stmt.ExpressionStmt;
//import com.github.javaparser.ast.stmt.Statement;


import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class MathExprGenerator extends BaseGenerator {



    public MathExprGenerator(Random r) {
        super(r);
    }

    public String generateCompString(boolean includeBitOps) {
        return generateCompString(new ArrayList<>(), rand.nextInt(3) + 2, rand.nextInt(3) + 2, includeBitOps);
    }
    public String generateCompString(ArrayList<String> vars, int numberOfVars, int numberOfLits, boolean includeBitOps) {
        return generateCompString(vars,numberOfVars,numberOfLits,includeBitOps,true);
    }

    public String generateCompString(ArrayList<String> vars, int numberOfVars, int numberOfLits, boolean includeBitOps, boolean includeIncDec) {

        String[] ops = {"+", "-", "*", "/"};    //, "%"};
        //String[] bitOps = {"&", "|", "~", "^", "<<", ">>", ">>>"};
        String[] types = {"float", "double", "int", "short", "long"};
        if(includeBitOps){
            types = new String[]{"int", "short", "long"};
            ops = new String[]{"/","&", "|", "^", "<<", ">>", ">>>", "+", "-", "*", "/","&", "|", "^", "<<", ">>", ">>>"};//"~",
        }

        String ret = "";

        if(vars.size() == 0){
            numberOfLits = numberOfLits + numberOfVars;
            numberOfVars = 0;
        }

        while (numberOfVars + numberOfLits > 0) {
            boolean addVar = false;
            if (numberOfVars > 0 && numberOfLits > 0) {
                addVar = rand.nextBoolean();
            } else if (numberOfVars > 0) {
                addVar = true;
            }
            String op = ops[rand.nextInt(ops.length)];
            if (addVar) {
                String var = vars.get(rand.nextInt(vars.size()));
                String prefix = "";
                String postfix = "";
                if(includeIncDec){
                switch (rand.nextInt(5)) {
                    case 1:
                        prefix = "++";
                        break;
                    case 2:
                        prefix = "--";
                        break;
                    case 3:
                        postfix = "++";
                        break;
                    case 4:
                        postfix = "--";
                        break;
                    default:
                        prefix = "";
                        postfix = "";
                }
                }
                //ret +=  " (int) "+computeUnaryOps(prefix, includeBitOps)+ prefix + var + postfix;
                ret +=  " "+computeUnaryOps(prefix, includeBitOps)+ prefix + var + postfix;
                numberOfVars--;
            } else {
                String val =  randomElement(types[rand.nextInt(types.length)], rand);
               // ret += " (int) "+computeUnaryOps(val,includeBitOps)+ val;
                ret += " "+computeUnaryOps(val,includeBitOps)+ val;
                numberOfLits--;
            }
            if (numberOfVars + numberOfLits != 0) {
                ret += " " + op + " ";
            }
        }
System.out.println("ret: "+ret);
        return ret;
    }

    private String computeUnaryOps(String prefix, boolean includeBitOps){
        String[] unaryOps = new String[] {"-","+"};
        if(includeBitOps){
            unaryOps = new String[] {"~","-","+"};
        }
        int unaryOpNumb = rand.nextInt(6);
        String unaryPrefix = "";
//        for (int i = 0; i < unaryOpNumb; i++){
//            String unOp = "";
//            if((i==0 && prefix.startsWith("-")) || unaryPrefix.startsWith("-")){
//                unOp = includeBitOps ?  new String[] {"~","+"}[rand.nextInt(2)] : "+";
//            }
//            else if((i==0 && prefix.startsWith("+")) || unaryPrefix.startsWith("+")){
//                unOp = includeBitOps ? new String[] {"~","-"}[rand.nextInt(2)] : "-";
//            }
//            else {
//                unOp = unaryOps[rand.nextInt(unaryOps.length)];
//            }
//            unaryPrefix = unOp+unaryPrefix;
//        }
        return unaryPrefix;
    }

    public Map<String, ExpressionStatement> genVarsWithInitStatement(AST a, int MaxNumber, Map<String, String> values, Map<String, String> varTypes, boolean includeBitOps){
        return genVarsWithInitStatement(a,MaxNumber,values,varTypes,includeBitOps,false);
    }
    public Map<String, ExpressionStatement> genVarsWithInitStatement(AST a, int MaxNumber, Map<String, String> values, Map<String, String> varTypes, boolean includeBitOps, boolean includeByteChar){
        Map<String,ExpressionStatement> vars = new HashMap<>();
        String[] types = { "double", "int", "short", "long"};//"float",
        if(includeBitOps) {
            types = new String[]{"int", "short", "long"};
        }
        if(includeByteChar){
            types = new String[]{ "double", "int", "short", "long", "float", "byte", "char"};
        }
        MaxNumber = rand.nextInt(MaxNumber)+2;
        for(int i = 0; i < MaxNumber;i++) {

            boolean isArray = false;//rand.nextBoolean(); //TODO Turn back on
            int nesting = rand.nextInt(2) + 1;
            String type = types[rand.nextInt(types.length)];
            String name = "v" + BaseGenerator.alphanumericStringGen(rand,7);

            //Boolean isvariable= (Boolean) getParameters().getOrDefault("variable",false);

            String value = randomElement(type, rand);

            String access = "";
            if(isArray && !type.equals("short")){
                Map<Integer,Integer> nestedArraySizes = new HashMap<>();
                value = nestedArrayConstruction(type,nesting,rand,nestedArraySizes);
                for(int j = 0; j < nesting; j++){
                    type+="[]";
                }
                int randomAccessInserts =  1;//rand.nextInt(1)+1;
                for(int k = 0; k < randomAccessInserts; k++){
                    access = name;//"System.out.println("+name;

                    List<Integer> sizes = new ArrayList<>();
                    sizes.addAll(nestedArraySizes.keySet());
                    Collections.sort(sizes, Collections.reverseOrder());
                    for(int j = 0; j < sizes.size(); j++){
                        int index = nestedArraySizes.get(sizes.get(j));
                        access += "["+rand.nextInt(index)+"]";
                    }
                    //access += ");";

                    System.out.println(access);
                }

            }

            if(values != null){
                values.put(name,value);
            }
            if(varTypes !=null)
            {
                varTypes.put(name,type);
            }

            VariableDeclarationFragment fragment = a.newVariableDeclarationFragment();
            fragment.setName(a.newSimpleName(name));
            VariableDeclarationExpression sde = a.newVariableDeclarationExpression(fragment);
            sde.setType(a.newPrimitiveType(PrimitiveType.toCode(type)));
            Assignment assignment = a.newAssignment();
            assignment.setLeftHandSide(sde);
            assignment.setRightHandSide(a.newNumberLiteral(value));
            ExpressionStatement est = a.newExpressionStatement(assignment);

            if(isArray && !type.equals("short")){
                vars.put(access,est);
            }
            else
            {
                vars.put(name,est);
            }
        }
        return vars;
    }


    public Map<String, SingleVariableDeclaration> genSingleVariableDeclaration(AST a, int MaxNumber, Map<String, String> varTypes){
        Map<String,SingleVariableDeclaration> vars = new HashMap<>();
        String[] types = new String[]{ "double", "int", "short", "long", "float", "byte", "char"};

        MaxNumber = rand.nextInt(MaxNumber)+2;
        for(int i = 0; i < MaxNumber;i++) {

            boolean isArray = false;//rand.nextBoolean(); //TODO Turn back on
            int nesting = rand.nextInt(2) + 1;
            String type = types[rand.nextInt(types.length)];
            String name = "v" + BaseGenerator.alphanumericStringGen(rand,7);

            //Boolean isvariable= (Boolean) getParameters().getOrDefault("variable",false);

//            String value = randomElement(type, rand);
//            String access = "";
//            if(isArray && !type.equals("short")){
//                Map<Integer,Integer> nestedArraySizes = new HashMap<>();
//                value = nestedArrayConstruction(type,nesting,rand,nestedArraySizes);
//                for(int j = 0; j < nesting; j++){
//                    type+="[]";
//                }
//                int randomAccessInserts =  1;//rand.nextInt(1)+1;
//                for(int k = 0; k < randomAccessInserts; k++){
//                    access = name;//"System.out.println("+name;
//
//                    List<Integer> sizes = new ArrayList<>();
//                    sizes.addAll(nestedArraySizes.keySet());
//                    Collections.sort(sizes, Collections.reverseOrder());
//                    for(int j = 0; j < sizes.size(); j++){
//                        int index = nestedArraySizes.get(sizes.get(j));
//                        access += "["+rand.nextInt(index)+"]";
//                    }
//                    //access += ");";
//
//                    System.out.println(access);
//                }
//
//            }

            if(varTypes !=null)
            {
                varTypes.put(name,type);
            }

            VariableDeclarationFragment fragment = a.newVariableDeclarationFragment();
            fragment.setName(a.newSimpleName(name));
            VariableDeclarationExpression sde = a.newVariableDeclarationExpression(fragment);
            sde.setType(a.newPrimitiveType(PrimitiveType.toCode(type)));

            var sd = a.newSingleVariableDeclaration();
            sd.setName(a.newSimpleName(name));
            sd.setType(a.newPrimitiveType(PrimitiveType.toCode(type)));


//            if(isArray && !type.equals("short")){
//                vars.put(access,est);
//            }
//            else
//            {
                vars.put(name,sd);
            //}
        }
        return vars;
    }

    String nestedArrayConstruction(String type, int nesting, Random rand, Map<Integer,Integer> nestedArraySizes){
        String ret = "";
        int elementCnt = rand.nextInt((3 - 1) + 1)  + 1;
        if(nestedArraySizes.containsKey(nesting)){
            nestedArraySizes.put(nesting,Math.min(nestedArraySizes.get(nesting),elementCnt));
        }
        else {
            nestedArraySizes.put(nesting, elementCnt);
        }
        nesting = nesting - 1;
        if(nesting == 0) {
            for(int i = 0; i < elementCnt;i++){
                String val;
                do {
                    val = randomElement(type, rand);
                } while(val.startsWith("0"));
                ret += val + ",";
            }
        }
        else if(nesting > 0){
            for(int i = 0; i < elementCnt;i++){
                ret +=  nestedArrayConstruction(type,nesting,rand, nestedArraySizes) + ",";
            }
        }
        else{
            assert false;
        }

        return "{"+ret.substring(0,ret.length()-1)+"}";
    }


}
