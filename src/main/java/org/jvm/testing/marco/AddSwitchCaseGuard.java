package org.jvm.testing.marco;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.jvm.testing.gen.BaseGenerator;
import org.jvm.testing.gen.BoolExprGenerator;
import org.jvm.testing.gen.MathExprGenerator;
import org.jvm.testing.util.JdtUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AddSwitchCaseGuard implements Macro {

    @Override
    public String apply(CompilationUnit cu, String src, Random rand) {
        final List<SwitchStatement> switchStmts = new ArrayList<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(SwitchStatement node) {
                switchStmts.add(node);
                return super.visit(node);
            }
        });


        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());


        for (SwitchStatement s: switchStmts) {
            ListRewrite blockRewrite;
            try {
                blockRewrite = rewrite.getListRewrite(s.getParent(), Block.STATEMENTS_PROPERTY);
            }catch (IllegalArgumentException iae)
            {
                System.out.println(iae);
                continue;
            }
            ListRewrite listRewrite= rewrite.getListRewrite(s, SwitchStatement.STATEMENTS_PROPERTY);
            SwitchCase newSwitchCase= s.getAST().newSwitchCase();
            //System.out.println("Switch TYPE::"+((SwitchCase)s.statements().get(0)).expressions().get(0));

            String switchType = "";
            if(s.getExpression().resolveTypeBinding() != null) {
                switchType = s.getExpression().resolveTypeBinding().getName();
            }

            if(switchType.toLowerCase().contains("object")){
                GuardedPattern gp =  s.getAST().newGuardedPattern();


//                InfixExpression ie = s.getAST().newInfixExpression();
//                ie.setLeftOperand(s.getAST().newName("i"));
//                ie.setRightOperand(s.getAST().newNumberLiteral("10000000"));
//                ie.setOperator(InfixExpression.Operator.GREATER);
//                gp.setExpression(ie);

//                VariableDeclarationFragment fragment = s.getAST().newVariableDeclarationFragment();
//                fragment.setName(s.getAST().newSimpleName("asdfasdf"));
//                VariableDeclarationExpression sde = s.getAST().newVariableDeclarationExpression(fragment);
//                sde.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("Integer")));
//                Assignment a = s.getAST().newAssignment();
//                a.setLeftHandSide(sde);
//                a.setRightHandSide(s.getAST().newNumberLiteral("1"));
//                ExpressionStatement est = s.getAST().newExpressionStatement(a);


                Map<String, ExpressionStatement> vars = new MathExprGenerator(rand).genVarsWithInitStatement(s.getAST(),3,null,null,false,false);
                ArrayList<String> varNames = new ArrayList<>(vars.keySet());

                for (String var: vars.keySet()) {
                    blockRewrite.insertFirst(vars.get(var), null);
                }

                String expr = new BoolExprGenerator(rand).genBoolString(varNames, 2,5,2,false,false);
                System.out.println(expr);
                expr = "("+expr + ") && "+ varNames.get(0) + " != " + varNames.get(0);
                Expression e = (Expression) JdtUtil.genASTFromSource(expr, "", JavaCore.VERSION_20, ASTParser.K_EXPRESSION);
                e = (Expression) ASTNode.copySubtree(s.getAST(),e);

                gp.setExpression(e);

                TypePattern tp = s.getAST().newTypePattern();

                var sd = s.getAST().newSingleVariableDeclaration();
                sd.setName(s.getAST().newSimpleName("o"+BaseGenerator.alphanumericStringGen(rand,7)));
                sd.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("Object")));
                tp.setPatternVariable(sd);
                gp.setPattern(tp);
                newSwitchCase.expressions().add(gp);
            }
            else{
                continue;
            }
            //newSwitchCase.

            if(((SwitchCase)s.statements().get(0)).isSwitchLabeledRule()) {
                newSwitchCase.setSwitchLabeledRule(true);
            }
//            MethodInvocation methodInvocation = s.getAST().newMethodInvocation();
//            QualifiedName qName =
//                    s.getAST().newQualifiedName(
//                            s.getAST().newSimpleName("System"),
//                            s.getAST().newSimpleName("out"));
//            methodInvocation.setExpression(qName);
//            methodInvocation.setName(s.getAST().newSimpleName("println"));
//
//            StringLiteral literal = s.getAST().newStringLiteral();
//            literal.setLiteralValue("Failed!");
//            methodInvocation.arguments().add(literal);


            ThrowStatement ts = s.getAST().newThrowStatement();

            ClassInstanceCreation cic = s.getAST().newClassInstanceCreation();
            StringLiteral literal = s.getAST().newStringLiteral();
            literal.setLiteralValue("Failed!");
            cic.arguments().add(literal);
            cic.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("AssertionError")));

            ts.setExpression(cic);



//            if(s.statements().size()> 1) {
//                SwitchCase last = (SwitchCase)s.statements().get(s.statements().size()-2);
//                if(last.isDefault()){
//                    listRewrite.insertAt(newSwitchCase, s.statements().size()-2, null);
//                    listRewrite.insertAt(ts, s.statements().size()-1, null);
//                }
//                else {
//                    listRewrite.insertLast(newSwitchCase, null);
//                    listRewrite.insertLast(ts, null);
//                }
//            }
//            else {
//                listRewrite.insertLast(newSwitchCase, null);
//                listRewrite.insertLast(ts, null);
//            }
            listRewrite.insertFirst(newSwitchCase, null);
            listRewrite.insertAt(ts,1, null);


//            if(!((SwitchCase)s.statements().get(0)).isSwitchLabeledRule()) {
//                listRewrite.insertAt(s.getAST().newBreakStatement(), 2, null);
//            }
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
        return src.contains("switch");
    }
}