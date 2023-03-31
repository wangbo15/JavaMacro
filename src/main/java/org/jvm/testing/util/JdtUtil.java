package org.jvm.testing.util;


import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.Map;

public class JdtUtil {

    private static ASTParser genASTParser(String src, String unitName, String jdkVersion, int astType){
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(jdkVersion, options);
        parser.setCompilerOptions(options);
        parser.setSource(src.toCharArray());
        parser.setKind(astType);

        parser.setEnvironment(null, null, null, true);
        parser.setUnitName(unitName); //需要与代码文件的名称一致
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);

        return parser;
    }

    public static ASTNode genASTFromSource(String src, String unitName, String jdkVersion, int astType) {
        ASTParser astParser = genASTParser(src, unitName, jdkVersion, astType);
        return astParser.createAST(null);
    }

    public static ASTNode genASTFromSource(String src, String jdkVersion, int astType) {
        ASTParser astParser = genASTParser(src, null, jdkVersion, astType);
        return astParser.createAST(null);
    }

    public static MethodDeclaration getMethodDeclarationByLine(final String filePath, final String jdkVersion, final int line){
        final CompilationUnit cu = (CompilationUnit) JdtUtil.genASTFromSource(FileUtil.readFileToString(filePath), jdkVersion, ASTParser.K_COMPILATION_UNIT);
        class MtdVisitor extends ASTVisitor {
            private MethodDeclaration hitMtd;

            public MethodDeclaration getHitMtd(){
                return hitMtd;
            }

            @Override
            public boolean visit(MethodDeclaration node) {
                if(cu.getLineNumber(node.getStartPosition()) <= line && cu.getLineNumber(node.getStartPosition() + node.getLength()) > line){
                    hitMtd = node;
                }
                return super.visit(node);
            }

        };
        MtdVisitor visitor = new MtdVisitor();
        cu.accept(visitor);
        return visitor.getHitMtd();
    }

    public static void switchOrder(ASTRewrite rewrite, ASTNode node0, ASTNode node1) {
        ASTNode noded0Copy = ASTNode.copySubtree(node0.getAST(), node0);
        ASTNode noded1Copy = ASTNode.copySubtree(node1.getAST(), node1);
        rewrite.replace(node0, noded1Copy, null);
        rewrite.replace(node1, noded0Copy, null);
    }

    public static Statement getParentStatement(Expression expr) {
        ASTNode node = expr;
        while (node != null && !(node instanceof Statement)) {
            node = node.getParent();
        }
        if (node instanceof Statement) {
            return (Statement) node;
        } else {
            return null;
        }
    }
}