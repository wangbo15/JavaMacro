package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReplaceFloatWithVectorApiRepresentation extends ReplaceWithVectorApiRepresentation implements Macro {

    @Override
    public String apply(CompilationUnit cu, String src, Random rand) {
        final List<VariableDeclarationFragment> vararrays = new ArrayList<>();
        final List<Assignment> aarrays = new ArrayList<>();
        cu.accept(new ASTVisitor() {

            @Override
            public boolean visit(Assignment node) {
                if(node.getLeftHandSide() instanceof SimpleName && node.getRightHandSide() instanceof InfixExpression)
                {
                    var inf = (InfixExpression)node.getRightHandSide();
                    aarrays.add(node);
                }

                return super.visit(node);
            }

            public boolean visit(VariableDeclarationFragment node) {

                if(node.getInitializer() instanceof  InfixExpression)
                {
                    var inf = (InfixExpression)node.getInitializer();
//                    System.out.println(inf.getLeftOperand().getClass());
//                    System.out.println(inf.getRightOperand().getClass());
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
            if(type.equalsIgnoreCase("float")){
                transform(rand,rewrite,d,type,d.getName().toString(),(InfixExpression) d.getInitializer());
            }
        }

        for(var d: aarrays){
            if(d.getLeftHandSide().resolveTypeBinding() == null || d.getLeftHandSide().resolveTypeBinding().getName() == null){
                continue;
            }
            String type =d.getLeftHandSide().resolveTypeBinding().getName().replace("[","").replace("]","");
            if(type.equalsIgnoreCase("float")){
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



    @Override
    public boolean isMacroApplicable(String src) {
        return src.contains("float");
    }
}