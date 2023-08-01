package org.jvm.testing.marco;

import com.sun.mirror.declaration.ClassDeclaration;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.jvm.testing.gen.MathExprGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AddVectorApiRepresentation implements Macro {

    @Override
    public String apply(CompilationUnit cu, String src, Random rand) {
        final List<VariableDeclarationFragment> arrays = new ArrayList<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(VariableDeclarationFragment node) {

                if(node.getInitializer() instanceof  ArrayCreation || node.getInitializer() instanceof ArrayInitializer)
                {
//                    System.out.println(node);
//                    System.out.println(node.getClass());
//                    System.out.println(node.getParent().getClass());
//                    System.out.println(node.getParent().getParent().getClass());
//                    System.out.println(node.getParent().getParent().getParent().getClass());
//                   System.out.println(node.getName());
////                    if(node.getInitializer() instanceof  ArrayInitializer) {
////                        System.out.println(""+((ArrayInitializer) node.getInitializer()));
////                    }
//
//                    System.out.println(((VariableDeclarationStatement)node.getParent()).getType());
                    //System.out.println(((VariableDeclarationFragment)node.getParent()).getInitializer().getClass());
                    arrays.add(node);
                }




//                if(node.getRightHandSide() instanceof ArrayCreation){
//                    arrayAssignments.add(node);
//                }

                return super.visit(node);
            }
//            @Override
//            public boolean visit(MethodInvocation node) {
//                System.out.println(node);
//                System.out.println(node.getAST());
//                System.out.println(node.getName());
//                System.out.println(node.getClass());
//                System.out.println(node.getParent().getClass());
//                System.out.println(node.getParent().getParent().getClass());
//
//
//                return super.visit(node);
//            }
        });


        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());


        for (var d: arrays) {
            if(!(d.getParent().getParent() instanceof Block)){
                continue;
            }
            ListRewrite listRewrite= rewrite.getListRewrite(d.getParent().getParent(), Block.STATEMENTS_PROPERTY);
            System.out.println(d);
            //IntVector.fromArray(IntVector.SPECIES_64, x, 0).intoArray(x,0);
//            MethodInvocation methodInvocation = d.getAST().newMethodInvocation();
//            //QualifiedName qName = d.getAST().newQualifiedName(d.getAST().newSimpleName("jdk"),d.getAST().newSimpleName("incubator"),d.getAST().newSimpleName("vector"),d.getAST().newSimpleName("IntVector"));
//            methodInvocation.setExpression(d.getAST().newSimpleName("jdk.incubator.vector.IntVector"));
//            methodInvocation.setName(d.getAST().newSimpleName("fromArray"));
//            methodInvocation.arguments().add(d.getAST().newQualifiedName(d.getAST().newSimpleName("IntVector"),d.getAST().newSimpleName("SPECIES_64")));
//            methodInvocation.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
//            methodInvocation.arguments().add(d.getAST().newNumberLiteral("0"));
//

            String type =d.getInitializer().resolveTypeBinding().getName().replace("[","").replace("]","");
            System.out.println(type);

            if(type.equalsIgnoreCase("int")) {
                MethodInvocation methodInvocation = d.getAST().newMethodInvocation();
                QualifiedName qName = d.getAST().newQualifiedName(
                                d.getAST().newName("jdk.incubator.vector"),
                                d.getAST().newSimpleName("IntVector"));
                methodInvocation.setExpression(qName);
                methodInvocation.setName(d.getAST().newSimpleName("fromArray"));

                methodInvocation.arguments().add(d.getAST().newQualifiedName(d.getAST().newName("jdk.incubator.vector.IntVector"), d.getAST().newSimpleName("SPECIES_64")));
                methodInvocation.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
                methodInvocation.arguments().add(d.getAST().newNumberLiteral("0"));

                MethodInvocation methodInvocation1 = d.getAST().newMethodInvocation();
                methodInvocation1.setExpression(methodInvocation);
                methodInvocation1.setName(d.getAST().newSimpleName("intoArray"));
                methodInvocation1.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
                methodInvocation1.arguments().add(d.getAST().newNumberLiteral("0"));
                listRewrite.insertAfter(d.getAST().newExpressionStatement(methodInvocation1), d.getParent(), null);
            }
            else if(type.equalsIgnoreCase("double")){
                MethodInvocation methodInvocation = d.getAST().newMethodInvocation();
                QualifiedName qName = d.getAST().newQualifiedName(
                        d.getAST().newName("jdk.incubator.vector"),
                        d.getAST().newSimpleName("DoubleVector"));
                methodInvocation.setExpression(qName);
                methodInvocation.setName(d.getAST().newSimpleName("fromArray"));

                methodInvocation.arguments().add(d.getAST().newQualifiedName(d.getAST().newName("jdk.incubator.vector.DoubleVector"), d.getAST().newSimpleName("SPECIES_64")));
                methodInvocation.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
                methodInvocation.arguments().add(d.getAST().newNumberLiteral("0"));

                MethodInvocation methodInvocation1 = d.getAST().newMethodInvocation();
                methodInvocation1.setExpression(methodInvocation);
                methodInvocation1.setName(d.getAST().newSimpleName("intoArray"));
                methodInvocation1.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
                methodInvocation1.arguments().add(d.getAST().newNumberLiteral("0"));
                listRewrite.insertAfter(d.getAST().newExpressionStatement(methodInvocation1), d.getParent(), null);
            }
            else if(type.equalsIgnoreCase("float")){
                MethodInvocation methodInvocation = d.getAST().newMethodInvocation();
                QualifiedName qName = d.getAST().newQualifiedName(
                        d.getAST().newName("jdk.incubator.vector"),
                        d.getAST().newSimpleName("FloatVector"));
                methodInvocation.setExpression(qName);
                methodInvocation.setName(d.getAST().newSimpleName("fromArray"));

                methodInvocation.arguments().add(d.getAST().newQualifiedName(d.getAST().newName("jdk.incubator.vector.FloatVector"), d.getAST().newSimpleName("SPECIES_64")));
                methodInvocation.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
                methodInvocation.arguments().add(d.getAST().newNumberLiteral("0"));

                MethodInvocation methodInvocation1 = d.getAST().newMethodInvocation();
                methodInvocation1.setExpression(methodInvocation);
                methodInvocation1.setName(d.getAST().newSimpleName("intoArray"));
                methodInvocation1.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
                methodInvocation1.arguments().add(d.getAST().newNumberLiteral("0"));
                listRewrite.insertAfter(d.getAST().newExpressionStatement(methodInvocation1), d.getParent(), null);
            }
            else if(type.equalsIgnoreCase("short")){
                MethodInvocation methodInvocation = d.getAST().newMethodInvocation();
                QualifiedName qName = d.getAST().newQualifiedName(
                        d.getAST().newName("jdk.incubator.vector"),
                        d.getAST().newSimpleName("ShortVector"));
                methodInvocation.setExpression(qName);
                methodInvocation.setName(d.getAST().newSimpleName("fromArray"));

                methodInvocation.arguments().add(d.getAST().newQualifiedName(d.getAST().newName("jdk.incubator.vector.ShortVector"), d.getAST().newSimpleName("SPECIES_64")));
                methodInvocation.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
                methodInvocation.arguments().add(d.getAST().newNumberLiteral("0"));

                MethodInvocation methodInvocation1 = d.getAST().newMethodInvocation();
                methodInvocation1.setExpression(methodInvocation);
                methodInvocation1.setName(d.getAST().newSimpleName("intoArray"));
                methodInvocation1.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
                methodInvocation1.arguments().add(d.getAST().newNumberLiteral("0"));
                listRewrite.insertAfter(d.getAST().newExpressionStatement(methodInvocation1), d.getParent(), null);
            }
            else if(type.equalsIgnoreCase("long")){
                MethodInvocation methodInvocation = d.getAST().newMethodInvocation();
                QualifiedName qName = d.getAST().newQualifiedName(
                        d.getAST().newName("jdk.incubator.vector"),
                        d.getAST().newSimpleName("LongVector"));
                methodInvocation.setExpression(qName);
                methodInvocation.setName(d.getAST().newSimpleName("fromArray"));

                methodInvocation.arguments().add(d.getAST().newQualifiedName(d.getAST().newName("jdk.incubator.vector.LongVector"), d.getAST().newSimpleName("SPECIES_64")));
                methodInvocation.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
                methodInvocation.arguments().add(d.getAST().newNumberLiteral("0"));

                MethodInvocation methodInvocation1 = d.getAST().newMethodInvocation();
                methodInvocation1.setExpression(methodInvocation);
                methodInvocation1.setName(d.getAST().newSimpleName("intoArray"));
                methodInvocation1.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
                methodInvocation1.arguments().add(d.getAST().newNumberLiteral("0"));
                listRewrite.insertAfter(d.getAST().newExpressionStatement(methodInvocation1), d.getParent(), null);
            }
            else if(type.equalsIgnoreCase("byte")){
                MethodInvocation methodInvocation = d.getAST().newMethodInvocation();
                QualifiedName qName = d.getAST().newQualifiedName(
                        d.getAST().newName("jdk.incubator.vector"),
                        d.getAST().newSimpleName("ByteVector"));
                methodInvocation.setExpression(qName);
                methodInvocation.setName(d.getAST().newSimpleName("fromArray"));

                methodInvocation.arguments().add(d.getAST().newQualifiedName(d.getAST().newName("jdk.incubator.vector.ByteVector"), d.getAST().newSimpleName("SPECIES_64")));
                methodInvocation.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
                methodInvocation.arguments().add(d.getAST().newNumberLiteral("0"));

                MethodInvocation methodInvocation1 = d.getAST().newMethodInvocation();
                methodInvocation1.setExpression(methodInvocation);
                methodInvocation1.setName(d.getAST().newSimpleName("intoArray"));
                methodInvocation1.arguments().add(d.getAST().newSimpleName(d.getName().toString()));
                methodInvocation1.arguments().add(d.getAST().newNumberLiteral("0"));
                listRewrite.insertAfter(d.getAST().newExpressionStatement(methodInvocation1), d.getParent(), null);
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




    @Override
    public boolean isMacroApplicable(String src) {
        return src.contains("int[]");
    }
}