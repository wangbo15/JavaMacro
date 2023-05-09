package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.jvm.testing.gen.BaseGenerator;
import org.jvm.testing.gen.MathExprGenerator;
import org.jvm.testing.util.JdtUtil;

import java.util.*;

public class AddRecords implements Macro {

    @Override
    public String apply(CompilationUnit cu, String src, Random rand) {
        final List<AbstractTypeDeclaration> declarations = new ArrayList<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
                declarations.add(node);
                return super.visit(node);
            }
            @Override
            public boolean visit(RecordDeclaration node) {
                declarations.add(node);
                return super.visit(node);
            }
        });


        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
        for (var d: declarations) {
            ListRewrite listRewrite;
            if(d instanceof TypeDeclaration){
                listRewrite= rewrite.getListRewrite(d,TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
            }
            else if(d instanceof RecordDeclaration){
                listRewrite= rewrite.getListRewrite(d,RecordDeclaration.BODY_DECLARATIONS_PROPERTY);
            }
            else{
                continue;
            }

            RecordDeclaration rd = d.getAST().newRecordDeclaration();
            rd.setName(d.getAST().newSimpleName("R"+BaseGenerator.alphanumericStringGen(rand,10)));


//            var sd = d.getAST().newSingleVariableDeclaration();
//            sd.setName(d.getAST().newSimpleName("o"+BaseGenerator.alphanumericStringGen(rand,7)));
//            sd.setType(d.getAST().newSimpleType(d.getAST().newSimpleName("Object")));

            Map<String, SingleVariableDeclaration> vars = new MathExprGenerator(rand).genSingleVariableDeclaration(d.getAST(),6,null);

            for (String var: vars.keySet()) {
                rd.recordComponents().add(vars.get(var));
            }



            listRewrite.insertFirst(rd, null);
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
        return true;
    }
}