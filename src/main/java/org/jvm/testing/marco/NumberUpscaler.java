package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.jvm.testing.util.FileUtil;
import org.jvm.testing.util.JdtUtil;

import java.util.*;

public class NumberUpscaler implements Macro {

    public static void main(String[] args) {
        String filePath = "/home/nightwish/workspace/compiler/testing/JavaMacro/Tests/src/main/java/org/example/Main.java";
        String fileUtilSrc = FileUtil.readFileToString(filePath);
        final CompilationUnit cu = (CompilationUnit) JdtUtil.genASTFromSource(fileUtilSrc, "1.7", ASTParser.K_COMPILATION_UNIT);

        NumberUpscaler nu = new NumberUpscaler();
        System.out.println(nu.apply(cu, fileUtilSrc));
    }


    @Override
    public String apply(CompilationUnit cu, String src) {
        final List<VariableDeclarationStatement> declStmts = new ArrayList<>();
        final List<SingleVariableDeclaration> singleDecls = new ArrayList<>();
        final Set<String> variableNames = new HashSet<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(VariableDeclarationStatement node) {
                declStmts.add(node);
                Iterator it = node.fragments().iterator();
                while(it.hasNext()) {
                    VariableDeclarationFragment vdf = (VariableDeclarationFragment) it.next();
                    vdf.getName().toString();
                }
                return super.visit(node);
            }

            @Override
            public boolean visit(SingleVariableDeclaration node) {
                singleDecls.add(node);
                return super.visit(node);
            }
        });

        final List<ASTNode> shouldNotExtend = new ArrayList<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(ArrayAccess node) {
                Expression expr = node.getIndex();

                expr.accept(new ASTVisitor() {
                    @Override
                    public boolean visit(SimpleName node) {
                        return super.visit(node);
                    }
                });
                return super.visit(node);
            }
        });

        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
        for (VariableDeclarationStatement decl: declStmts) {
            Type tp = decl.getType();
            if (tp.toString().equals("int")) {
                Type newType = decl.getAST().newPrimitiveType(PrimitiveType.LONG);
                rewrite.set(decl, VariableDeclarationStatement.TYPE_PROPERTY, newType, null);
            }
        }

        for (SingleVariableDeclaration svd : singleDecls) {
            Type tp = svd.getType();
            if (tp.toString().equals("int")) {
                Type newType = svd.getAST().newPrimitiveType(PrimitiveType.LONG);
                rewrite.set(svd, SingleVariableDeclaration.TYPE_PROPERTY, newType, null);
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
}
