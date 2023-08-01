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

public class AddNullSwitchCase implements Macro {

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
            boolean nullCaseExists = false;
            for (Object s1:s.statements()) {
                if(s1 instanceof SwitchCase){
                    if(((SwitchCase)s1).expressions().size() >0){
                        if(((SwitchCase)s1).expressions().get(0) instanceof NullLiteral){
                            nullCaseExists = true;
                            break;
                        }
                    }
                }
            }
            if(nullCaseExists){
                continue;
            }
            ListRewrite listRewrite= rewrite.getListRewrite(s, SwitchStatement.STATEMENTS_PROPERTY);
            SwitchCase newSwitchCase= s.getAST().newSwitchCase();

            NullLiteral sl = s.getAST().newNullLiteral();
            newSwitchCase.expressions().add(sl);

            if(s.statements().size() > 0 && ((SwitchCase)s.statements().get(0)).isSwitchLabeledRule()) {
                newSwitchCase.setSwitchLabeledRule(true);
            }

            listRewrite.insertFirst(newSwitchCase, null);

            ThrowStatement ts = s.getAST().newThrowStatement();

            ClassInstanceCreation cic = s.getAST().newClassInstanceCreation();
            StringLiteral literal1 = s.getAST().newStringLiteral();
            literal1.setLiteralValue("Failed!");
            cic.arguments().add(literal1);
            cic.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("AssertionError")));

            ts.setExpression(cic);

            listRewrite.insertAt(ts, 1, null);
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