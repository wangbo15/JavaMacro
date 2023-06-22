package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.jvm.testing.gen.BaseGenerator;
import org.jvm.testing.gen.MathExprGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AddMethodsToRecords implements Macro {

    @Override
    public String apply(CompilationUnit cu, String src, Random rand) {
        final List<RecordDeclaration> declarations = new ArrayList<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(RecordDeclaration node) {
                declarations.add(node);
                return super.visit(node);
            }
        });


        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());


        for (var d: declarations) {
            ListRewrite listRewrite= rewrite.getListRewrite(d,RecordDeclaration.BODY_DECLARATIONS_PROPERTY);


//                Map<String, ExpressionStatement> vars = new MathExprGenerator(rand).genVarsWithInitStatement(d.getAST(),3,null,null,false,false);
//                ArrayList<String> varNames = new ArrayList<>(vars.keySet());
            int maxAddMethodNumber = rand.nextInt(3)+1;
            while (maxAddMethodNumber-- > 0) {

                MethodDeclaration md = d.getAST().newMethodDeclaration();
                md.setName(d.getAST().newSimpleName("fun" + BaseGenerator.alphanumericStringGen(rand, 10)));

//                for (String var: vars.keySet()) {
//                    ((VariableDeclarationExpression)((Assignment) vars.get(var).getExpression()).getLeftHandSide()).modifiers().add(d.getAST().newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
//                    int addModifiers = rand.nextInt(3);
//                    ArrayList<Modifier.ModifierKeyword> mods = new ArrayList<>();
//                    //mods.add(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
//                    //mods.add(Modifier.ModifierKeyword.STATIC_KEYWORD);
//                    mods.add(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
//                    mods.add(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
//                    mods.add(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
//
//                    mods.add(Modifier.ModifierKeyword.FINAL_KEYWORD);
//                    //mods.add(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
//                    //mods.add(Modifier.ModifierKeyword.DEFAULT_KEYWORD);
//                    //mods.add(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
//                    mods.add(Modifier.ModifierKeyword.SEALED_KEYWORD);
//
//
//                    while (addModifiers-- >0){
//                        var m = mods.get(rand.nextInt(mods.size()));
//
//                        ((VariableDeclarationExpression)((Assignment) vars.get(var).getExpression()).getLeftHandSide()).modifiers().add(d.getAST().newModifier(m));
//                        if(m.equals(Modifier.ModifierKeyword.PUBLIC_KEYWORD) || m.equals(Modifier.ModifierKeyword.PRIVATE_KEYWORD) || m.equals(Modifier.ModifierKeyword.PROTECTED_KEYWORD)){
//                            mods.remove(2);
//                            mods.remove(1);
//                            mods.remove(0);
//                        }
//
//                    }


                listRewrite.insertFirst(md, null);

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
        return src.contains("record");
    }
}