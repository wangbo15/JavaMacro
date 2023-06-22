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

public class AddModifiersToRecords implements Macro {

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
            ListRewrite listRewrite= rewrite.getListRewrite(d,RecordDeclaration.MODIFIERS2_PROPERTY);

            int addModifiers = 2;//rand.nextInt(2);
            ArrayList<Modifier.ModifierKeyword> mods = new ArrayList<>();
//            mods.add(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
//            mods.add(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
//            mods.add(Modifier.ModifierKeyword.PUBLIC_KEYWORD);

            mods.add(Modifier.ModifierKeyword.FINAL_KEYWORD);
           // mods.add(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
            mods.add(Modifier.ModifierKeyword.STATIC_KEYWORD);
            //d.modifiers().clear();

//            for (Object o: d.modifiers()) {
//                Modifier m = (Modifier) o;
//                listRewrite.remove(m,null);
//            }


            //listRewrite.remove(d.getAST().newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
            while (addModifiers-- > 0) {
                var m = mods.get(rand.nextInt(mods.size()));

                //if(!d.modifiers().contains(m)){
                    listRewrite.insertFirst(d.getAST().newModifier(m),null);
                //}

//                if(m.equals(Modifier.ModifierKeyword.PUBLIC_KEYWORD) || m.equals(Modifier.ModifierKeyword.PRIVATE_KEYWORD) || m.equals(Modifier.ModifierKeyword.PROTECTED_KEYWORD)){
//                    mods.remove(2);
//                    mods.remove(1);
//                    mods.remove(0);
//                }
//                else{
                    mods.remove(m);
               // }

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