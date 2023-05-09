package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.jvm.testing.gen.BaseGenerator;
import org.jvm.testing.util.JdtUtil;

import java.util.*;

public class AddSwitchCase implements Macro {

    @Override
    public String apply(CompilationUnit cu, String src, Random rand) {
        final List<SwitchStatement> switchStmts = new ArrayList<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(SwitchStatement node) {
                switchStmts.add(node);
                return super.visit(node);
            }
        });


        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
        for (SwitchStatement s: switchStmts) {
            ListRewrite listRewrite= rewrite.getListRewrite(s, SwitchStatement.STATEMENTS_PROPERTY);
            SwitchCase newSwitchCase= s.getAST().newSwitchCase();
            //System.out.println("Switch TYPE::"+((SwitchCase)s.statements().get(0)).expressions().get(0));
            String switchType = "";
            if(s.getExpression().resolveTypeBinding() != null) {
                switchType = s.getExpression().resolveTypeBinding().getName();
            }

            if(switchType.toLowerCase().contains("string")){
                StringLiteral sl = s.getAST().newStringLiteral();
                sl.setLiteralValue(BaseGenerator.stringGenWithoutQuotation(rand));
                newSwitchCase.expressions().add(sl);
            }
            else if(switchType.toLowerCase().contains("int")){
                NumberLiteral sl = s.getAST().newNumberLiteral(BaseGenerator.intGen(rand));
                newSwitchCase.expressions().add(sl);
            }
            else if(switchType.toLowerCase().contains("char")){
                CharacterLiteral sl = s.getAST().newCharacterLiteral();
                sl.setCharValue(BaseGenerator.charGen(rand).charAt(0));
                newSwitchCase.expressions().add(sl);
            }
            else if(switchType.toLowerCase().contains("short")){
                NumberLiteral sl = s.getAST().newNumberLiteral(BaseGenerator.shortGen(rand));
                newSwitchCase.expressions().add(sl);
            }
            else if(switchType.toLowerCase().contains("byte")){
                NumberLiteral sl = s.getAST().newNumberLiteral(BaseGenerator.byteGen(rand));
                newSwitchCase.expressions().add(sl);
            }
            else if(switchType.toLowerCase().contains("object")){
                NullLiteral sl = s.getAST().newNullLiteral();
                newSwitchCase.expressions().add(sl);
            }
            else{
                continue;
            }

            if(s.statements().size() > 0 && ((SwitchCase)s.statements().get(0)).isSwitchLabeledRule()) {
                newSwitchCase.setSwitchLabeledRule(true);
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

            listRewrite.insertFirst(newSwitchCase, null);

            ThrowStatement ts = s.getAST().newThrowStatement();

            ClassInstanceCreation cic = s.getAST().newClassInstanceCreation();
            StringLiteral literal1 = s.getAST().newStringLiteral();
            literal1.setLiteralValue("Failed!");
            cic.arguments().add(literal1);
            cic.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("AssertionError")));

            ts.setExpression(cic);

//            MethodInvocation exception = s.getAST().newMethodInvocation();
//            QualifiedName qName =
//                    s.getAST().newQualifiedName(
//                            s.getAST().newSimpleName("System"),
//                            s.getAST().newSimpleName("out"));
//            exception.setExpression(s.getAST().newSimpleName("new "));
//            exception.setName();
//




            listRewrite.insertAt(ts, 1, null);


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