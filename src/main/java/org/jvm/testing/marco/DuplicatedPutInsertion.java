package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import java.util.*;

public class DuplicatedPutInsertion implements Macro {
    public static final Set<String> MAP_TYPES = new HashSet<>();

    static {
        MAP_TYPES.add("Map");
        MAP_TYPES.add("HashMap");
        MAP_TYPES.add("Hashtable");
        MAP_TYPES.add("LinkedHashMap");

        MAP_TYPES.add("SortedMap");
        MAP_TYPES.add("TreeMap");
    }

    @Override
    public String apply(CompilationUnit cu, String src, Random rand) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());

        final List<Statement> statements = new ArrayList<>();

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                if (node.getParent() instanceof Statement == false
                        || !node.getName().toString().equals("put")
                        || node.getExpression() instanceof SimpleName == false) {
                    return true;
                }
                SimpleName sn = (SimpleName) node.getExpression();
                IVariableBinding binding = (IVariableBinding) sn.resolveBinding();
                if (binding != null) {
                    ITypeBinding itb = binding.getType();
                    String tp = itb.isParameterizedType() ? itb.getTypeDeclaration().getName() : itb.getName();
                    if (MAP_TYPES.contains(tp)) {
                        statements.add((Statement) node.getParent());
                    }
                }
                return true;
            }
        });

        for (Statement statement: statements) {
            Statement statementCopy = (Statement) ASTNode.copySubtree(statement.getAST(), statement);
            if (statement.getParent() instanceof Block) {
                Block block = (Block) statement.getParent();
                ListRewrite lrw = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
                lrw.insertAfter(statementCopy, statement, null);
            }
        }

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
        return src.contains(".put(");
    }
}
