package org.jvm.testing.util;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaProject;

import java.util.Hashtable;
import java.util.Map;

public class JdtUtil {

    private static ASTParser genASTParser(String src, String unitName, String jdkVersion, int astType) {
        //org.eclipse.jdt.core version 3.34 should be added to the pom file when it becomes available.
        // Alternatively, jar files from the eclipse (eclipse-SDK-I20230423-1800-linux-gtk-x86_64) should be placed in the lib folder,
        // or it can be switched back to JDT 3.33 which only supports Java 19.
        ASTParser parser = ASTParser.newParser(Integer.parseInt(jdkVersion));//AST.JLS20);
        Hashtable<String, String> options = JavaCore.getOptions();
        //options.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
        //options.put(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);

        options.put(CompilerOptions.OPTION_EnablePreviews,CompilerOptions.ENABLED);
        //options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.latestSupportedJavaVersion());
        options.put(JavaCore.COMPILER_SOURCE, jdkVersion);//JavaCore.VERSION_20);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, jdkVersion);//JavaCore.VERSION_20);
        options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);

        //long jdkLevel = CompilerOptions.versionToJdkLevel(JavaCore.latestSupportedJavaVersion());
        //String compliance = CompilerOptions.versionFromJdkLevel(jdkLevel);
        //System.out.println("compl:"+compliance);
        //JavaCore.setComplianceOptions(jdkVersion, options);


        //System.out.println(CompilerOptions.getLatestVersion()+""+JavaCore.latestSupportedJavaVersion()+""+JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES+ " "+ CompilerOptions.OPTION_EnablePreviews);
        parser.setCompilerOptions(options);
        parser.setSource(src.toCharArray());
        parser.setKind(astType);

        parser.setEnvironment(null, null, null, true);
        parser.setUnitName(unitName); //需要与代码文件的名称一致
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);

//
//        try {
//            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//            IProject project = root.getProject("temp");
//            project.create(null);
//            project.open(null);
//
//
//            IProjectDescription description = project.getDescription();
//            description.setNatureIds(new String[] { JavaCore.NATURE_ID });
//            project.setDescription(description, null);
//
//            IJavaProject jp = JavaCore.create(project);
//
//            jp.setOptions(options);
//            parser.setProject(jp);
//        } catch (CoreException e) {
//            e.printStackTrace();
//        }
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


    public static MethodDeclaration getMethodDeclarationByLine(final String filePath, final String jdkVersion, final int line) {
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