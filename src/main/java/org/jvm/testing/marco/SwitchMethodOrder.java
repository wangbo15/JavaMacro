package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.jvm.testing.util.FileUtil;
import org.jvm.testing.util.JdtUtil;

import java.util.Random;

public class SwitchMethodOrder implements Macro {

    @Override
    public String apply(CompilationUnit cu, String src, Random rand) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        final TypeDeclaration[] td = new TypeDeclaration[1];
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
                td[0] = node;
                return false;
            }
        });
        if (td[0] == null || td[0].isInterface() || td[0].getMethods() == null || td[0].getMethods().length < 2) {
            return src;
        }
        MethodDeclaration md0 = td[0].getMethods()[0];
        MethodDeclaration md1 = td[0].getMethods()[1];

        JdtUtil.switchOrder(rewriter, md0, md1);

        Document doc = new Document(src);
        TextEdit edits = rewriter.rewriteAST(doc, null);

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
        return true;
    }
}