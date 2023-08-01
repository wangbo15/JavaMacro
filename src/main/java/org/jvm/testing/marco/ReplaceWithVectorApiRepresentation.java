package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.jvm.testing.gen.BaseGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class ReplaceWithVectorApiRepresentation implements Macro {

    @Override
    public String apply(CompilationUnit cu, String src, Random rand) {
        final List<VariableDeclarationFragment> vararrays = new ArrayList<>();
        final List<Assignment> aarrays = new ArrayList<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(Assignment node) {
                if(node.getLeftHandSide() instanceof SimpleName && node.getRightHandSide() instanceof InfixExpression) {
                    var inf = (InfixExpression)node.getRightHandSide();
                    aarrays.add(node);
                }

                return super.visit(node);
            }

            public boolean visit(VariableDeclarationFragment node) {

                if(node.getInitializer() instanceof  InfixExpression) {
                    var inf = (InfixExpression)node.getInitializer();
                    vararrays.add(node);
                }
                return super.visit(node);
            }
        });


        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());


        for (var d: vararrays) {
            if(d.getInitializer().resolveTypeBinding() == null || d.getInitializer().resolveTypeBinding().getName() == null){
                continue;
            }
            String type =d.getInitializer().resolveTypeBinding().getName().replace("[","").replace("]","");
            if(type.equalsIgnoreCase("long")){
                transform(rand,rewrite,d,type,d.getName().toString(),(InfixExpression) d.getInitializer());
            }
        }

        for(var d: aarrays){
            if(d.getLeftHandSide().resolveTypeBinding() == null || d.getLeftHandSide().resolveTypeBinding().getName() == null){
                continue;
            }
            String type =d.getLeftHandSide().resolveTypeBinding().getName().replace("[","").replace("]","");
            if(type.equalsIgnoreCase("long")){
                transform(rand,rewrite,d,type,d.getLeftHandSide().toString(),(InfixExpression) d.getRightHandSide());
            }
        }

        Document doc = new Document(src);
        TextEdit edits = rewrite.rewriteAST(doc, null);
        String res = src;
        try {
            edits.apply(doc);
            res = doc.get();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return res;
    }

    protected void transform(Random rand, ASTRewrite rewrite, ASTNode node, String type, String name, InfixExpression inf){
        if(!(node.getParent().getParent() instanceof Block)) {
            return;
        }
        ListRewrite listRewrite= rewrite.getListRewrite(node.getParent().getParent(), Block.STATEMENTS_PROPERTY);

        String var1 = "var1"+ BaseGenerator.alphanumericStringGen(rand, 5);
        String var2 = "var2"+ BaseGenerator.alphanumericStringGen(rand, 5);
        String var3 = "var3"+ BaseGenerator.alphanumericStringGen(rand, 5);
        var m1 = transformToVector(type,inf.getLeftOperand(),var1,node);
        listRewrite.insertBefore(node.getAST().newExpressionStatement(m1), node.getParent(), null);
        var m2 = transformToVector(type,inf.getRightOperand(),var2,node);
        listRewrite.insertBefore(node.getAST().newExpressionStatement(m2), node.getParent(), null);


        VariableDeclarationFragment fragment = node.getAST().newVariableDeclarationFragment();
        fragment.setName(node.getAST().newSimpleName(var3));
        VariableDeclarationExpression sde = node.getAST().newVariableDeclarationExpression(fragment);
        sde.setType(node.getAST().newSimpleType(node.getAST().newSimpleName("var")));
        Assignment assignment = node.getAST().newAssignment();
        assignment.setLeftHandSide(sde);

        MethodInvocation methodInvocation = node.getAST().newMethodInvocation();
        methodInvocation.setExpression(node.getAST().newSimpleName(var1));
        //System.out.println("OP:"+inf.getOperator().toString());
        switch (inf.getOperator().toString()){
            case "+": methodInvocation.setName(node.getAST().newSimpleName("add"));
                break;
            case "-": methodInvocation.setName(node.getAST().newSimpleName("sub"));
                break;
            case "*": methodInvocation.setName(node.getAST().newSimpleName("mul"));
                break;
            case "/": methodInvocation.setName(node.getAST().newSimpleName("div"));
                break;
            case "&": methodInvocation.setName(node.getAST().newSimpleName("and"));
                break;
            case "^": methodInvocation.setName(node.getAST().newSimpleName("or"));
                break;
            case "==": methodInvocation.setName(node.getAST().newSimpleName("eq"));
                break;
            case "<": methodInvocation.setName(node.getAST().newSimpleName("lt"));
                break;
            case ">": methodInvocation.setName(node.getAST().newSimpleName("gt"));
                break;
            default:methodInvocation.setName(node.getAST().newSimpleName("add"));
        }

        methodInvocation.arguments().add(node.getAST().newSimpleName(var2));
        assignment.setRightHandSide(methodInvocation);
        listRewrite.insertBefore(node.getAST().newExpressionStatement(assignment), node.getParent(), null);

        Assignment assignment1 = node.getAST().newAssignment();
        assignment1.setLeftHandSide(node.getAST().newSimpleName(name));
        MethodInvocation methodInvocation1 = node.getAST().newMethodInvocation();
        methodInvocation1.setExpression(node.getAST().newSimpleName(var3));
        methodInvocation1.setName(node.getAST().newSimpleName("lane"));
        methodInvocation1.arguments().add(node.getAST().newNumberLiteral("0"));
        assignment1.setRightHandSide(methodInvocation1);
        listRewrite.insertAfter(node.getAST().newExpressionStatement(assignment1), node.getParent(), null);

    }

    protected Assignment transformToVector(String type, Expression exp, String name, ASTNode node){
        type = type.toLowerCase().replace("[","").replace("]","");
        type = type.substring(0,1).toUpperCase() + type.substring(1);
        //System.out.println(type);

        VariableDeclarationFragment fragment = node.getAST().newVariableDeclarationFragment();
        fragment.setName(node.getAST().newSimpleName(name));
        VariableDeclarationExpression sde = node.getAST().newVariableDeclarationExpression(fragment);
        sde.setType(node.getAST().newSimpleType(node.getAST().newSimpleName("var")));
        Assignment assignment = node.getAST().newAssignment();
        assignment.setLeftHandSide(sde);

        MethodInvocation methodInvocation = node.getAST().newMethodInvocation();
        QualifiedName qName = node.getAST().newQualifiedName(
                node.getAST().newName("jdk.incubator.vector"),
                node.getAST().newSimpleName(type+"Vector"));
        methodInvocation.setExpression(qName);
        methodInvocation.setName(node.getAST().newSimpleName("fromArray"));

        methodInvocation.arguments().add(node.getAST().newQualifiedName(node.getAST().newName("jdk.incubator.vector."+type+"Vector"), node.getAST().newSimpleName("SPECIES_64")));
        exp = (Expression) ASTNode.copySubtree(node.getAST(),exp);
        ArrayCreation ac = node.getAST().newArrayCreation();
        ac.setType(node.getAST().newArrayType(node.getAST().newPrimitiveType(PrimitiveType.toCode(type.toLowerCase()))));
        var ai = node.getAST().newArrayInitializer();
        ai.expressions().add(exp);
        if(!type.equalsIgnoreCase("long") && !type.equalsIgnoreCase("double"))
        {
            ai.expressions().add(node.getAST().newNumberLiteral("0"));
        }
        if(type.equalsIgnoreCase("short")){
            ai.expressions().add(node.getAST().newNumberLiteral("0"));
            ai.expressions().add(node.getAST().newNumberLiteral("0"));
        }
        if(type.equalsIgnoreCase("byte")){
            ai.expressions().add(node.getAST().newNumberLiteral("0"));
            ai.expressions().add(node.getAST().newNumberLiteral("0"));
            ai.expressions().add(node.getAST().newNumberLiteral("0"));
            ai.expressions().add(node.getAST().newNumberLiteral("0"));
            ai.expressions().add(node.getAST().newNumberLiteral("0"));
            ai.expressions().add(node.getAST().newNumberLiteral("0"));
        }
        ac.setInitializer(ai);
        methodInvocation.arguments().add(ac);
        methodInvocation.arguments().add(node.getAST().newNumberLiteral("0"));
        assignment.setRightHandSide(methodInvocation);

        return assignment;
    }




    @Override
    public boolean isMacroApplicable(String src) {
        return true;
    }
}