package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.jvm.testing.util.JdtUtil;
import org.jvm.testing.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class ExchangeOrderForCommutatives implements Macro {

    @Override
    public String apply(CompilationUnit cu, String src) {
        final List<InfixExpression> commutativeExpressions = new ArrayList<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(InfixExpression node) {
                if (node.getOperator().equals(InfixExpression.Operator.PLUS)) {
                    node.getLeftOperand().resolveTypeBinding();

                } else if (node.getOperator().equals(InfixExpression.Operator.TIMES) ||
                        node.getOperator().equals(InfixExpression.Operator.AND) ||
                        node.getOperator().equals(InfixExpression.Operator.OR)) {
                    commutativeExpressions.add(node);
                }
                return true;
            }
        });

        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
        for (InfixExpression exp: commutativeExpressions) {
            Expression op0 = exp.getLeftOperand();
            Expression op1 = exp.getRightOperand();
            JdtUtil.switchOrder(rewrite, op0, op1);
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
}