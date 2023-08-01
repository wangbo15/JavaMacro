package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.jvm.testing.gen.BaseGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AddSwitchLabels implements Macro {

    @Override
    public String apply(CompilationUnit cu, String src, Random rand) {
        final List<SwitchCase> switchCases = new ArrayList<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(SwitchCase node) {
                switchCases.add(node);
                return super.visit(node);
            }
        });


        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
        for (SwitchCase s: switchCases) {
            ListRewrite listRewrite= rewrite.getListRewrite(s, SwitchCase.EXPRESSIONS2_PROPERTY);
            //ListRewrite listRewrite= rewrite.getListRewrite(s, SwitchStatement.STATEMENTS_PROPERTY);

            String switchType = "";
            if(s.getParent() instanceof SwitchExpression){
                if(((SwitchExpression)s.getParent()).resolveTypeBinding() != null) {
                    switchType = ((SwitchExpression)s.getParent()).resolveTypeBinding().getName();
                }

            }else if(((SwitchStatement)s.getParent()).getExpression().resolveTypeBinding() != null) {
                switchType = ((SwitchStatement)s.getParent()).getExpression().resolveTypeBinding().getName();
            }


            if(s.isDefault()){
                continue;
            }

            if(switchType.toLowerCase().contains("string")){
                StringLiteral sl = s.getAST().newStringLiteral();
                sl.setLiteralValue(BaseGenerator.stringGenWithoutQuotation(rand));
                //s.expressions().add(sl);
                listRewrite.insertLast(sl,null);
            }
            else if(switchType.toLowerCase().contains("int")){
                NumberLiteral sl = s.getAST().newNumberLiteral(BaseGenerator.intGen(rand));
                listRewrite.insertLast(sl,null);
            }
            else if(switchType.toLowerCase().contains("char")){
                CharacterLiteral sl = s.getAST().newCharacterLiteral();
                sl.setCharValue(BaseGenerator.charGen(rand).charAt(0));
                listRewrite.insertLast(sl,null);
            }
            else if(switchType.toLowerCase().contains("short")){
                NumberLiteral sl = s.getAST().newNumberLiteral(BaseGenerator.shortGen(rand));
                listRewrite.insertLast(sl,null);
            }
            else if(switchType.toLowerCase().contains("byte")){
                NumberLiteral sl = s.getAST().newNumberLiteral(BaseGenerator.byteGen(rand));
                listRewrite.insertLast(sl,null);
            }
            else if(switchType.toLowerCase().contains("object")){
                System.out.println("Object Switch Case: "+s.toString());
                System.out.println(s.expressions().size());
                System.out.println(s.isSwitchLabeledRule());
                System.out.println(s.getProperty("when"));
                System.out.println(s.properties().size());
                System.out.println(s.getNodeType());
                System.out.println(s.getRoot().properties().size());
                System.out.println(s.expressions().get(0));
                System.out.println(s.expressions().get(0).getClass());
                if(s.expressions().get(0) instanceof GuardedPattern) {
                    System.out.println(((GuardedPattern) s.expressions().get(0)).getExpression());
                    System.out.println(((GuardedPattern) s.expressions().get(0)).getExpression().getClass());
                    System.out.println(((GuardedPattern) s.expressions().get(0)).getPattern());
                    System.out.println(((GuardedPattern) s.expressions().get(0)).getPattern().getClass());
                }
                //System.out.println(s.getStructuralProperty());
//                NullLiteral sl = s.getAST().newNullLiteral();
//                listRewrite.insertLast(sl,null);
                continue;
            }
            else{
                continue;
            }



//            MethodInvocation methodInvocation = s.getAST().newMethodInvocation();
//            QualifiedName qName =
//                    s.getAST().newQualifiedName(
//                            s.getAST().newSimpleName("System"),
//                            s.getAST().newSimpleName("out"));
//            methodInvocation.setExpression(qName);
//            methodInvocation.setName(s.getAST().newSimpleName("println"));
//
//            StringLiteral literal = s.getAST().newStringLiteral();
//            literal.setLiteralValue("Failed!");
//            methodInvocation.arguments().add(literal);
//
//            listRewrite.insertFirst(newSwitchCase, null);
//            listRewrite.insertAt(s.getAST().newExpressionStatement(methodInvocation), 1, null);
//            if(!((SwitchCase)s.statements().get(0)).isSwitchLabeledRule()) {
//                listRewrite.insertAt(s.getAST().newBreakStatement(), 2, null);
//            }
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
        return src.contains("switch");
    }
}