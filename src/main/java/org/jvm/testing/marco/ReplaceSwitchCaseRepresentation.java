package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.jvm.testing.gen.BaseGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReplaceSwitchCaseRepresentation implements Macro {

    @Override
    public String apply(CompilationUnit cu, String src, Random rand) {
        final List<SwitchCase> switchCases = new ArrayList<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(SwitchCase node) {
                switchCases.add(node);
                return super.visit(node);
            }
        });

        boolean randomMode = true;


        ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
        for (SwitchCase s: switchCases) {
            ListRewrite listRewrite= rewrite.getListRewrite(s, SwitchCase.EXPRESSIONS2_PROPERTY);
            //ListRewrite listRewrite= rewrite.getListRewrite(s, SwitchStatement.STATEMENTS_PROPERTY);

            String switchType = "";
            if(((SwitchStatement)s.getParent()).getExpression().resolveTypeBinding() != null) {
                switchType = ((SwitchStatement)s.getParent()).getExpression().resolveTypeBinding().getName();
            }

            if(s.isDefault()){
                continue;
            }

            if(randomMode && rand.nextBoolean()){
                continue;
            }

            if(switchType.toLowerCase().contains("string")){
                System.out.println("CaseType:"+s.expressions().get(0).getClass());
                if(s.expressions().get(0) instanceof StringLiteral && s.expressions().size() == 1){
                    var gp =  s.getAST().newGuardedPattern();
                    var sd = s.getAST().newSingleVariableDeclaration();
                    String name = "s"+BaseGenerator.alphanumericStringGen(rand,7);
                    sd.setName(s.getAST().newSimpleName(name));
                    sd.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("String")));
                    TypePattern tp = s.getAST().newTypePattern();
                    tp.setPatternVariable(sd);
                    gp.setPattern(tp);
                    MethodInvocation methodInvocation = s.getAST().newMethodInvocation();
                    //QualifiedName qName = s.getAST().newQualifiedName();
                    methodInvocation.setExpression(s.getAST().newSimpleName(name));
                    methodInvocation.setName(s.getAST().newSimpleName("equals"));

                    StringLiteral literal = s.getAST().newStringLiteral();
                    literal.setLiteralValue(((StringLiteral)s.expressions().get(0)).getLiteralValue());
                    methodInvocation.arguments().add(literal);
                    gp.setExpression(methodInvocation);

                    listRewrite.replace((ASTNode) s.expressions().get(0),gp,null);
                }
            }
            else if(switchType.toLowerCase().contains("int")){
                if(s.expressions().get(0) instanceof NumberLiteral && s.expressions().size() == 1){
                    if(!(((SwitchStatement)s.getParent()).getExpression() instanceof CastExpression)){
                        var cast = s.getAST().newCastExpression();
                        cast.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("Integer")));

                        var exp = (Expression) ASTNode.copySubtree(s.getAST(),((SwitchStatement)s.getParent()).getExpression());
                        cast.setExpression(exp);
                        rewrite.set(s.getParent(),SwitchStatement.EXPRESSION_PROPERTY,cast,null);
                    }
                    var gp =  s.getAST().newGuardedPattern();
                    var sd = s.getAST().newSingleVariableDeclaration();
                    String name = "i"+BaseGenerator.alphanumericStringGen(rand,7);
                    sd.setName(s.getAST().newSimpleName(name));
                    sd.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("Integer")));
                    TypePattern tp = s.getAST().newTypePattern();
                    tp.setPatternVariable(sd);
                    gp.setPattern(tp);

                    InfixExpression ie = s.getAST().newInfixExpression();
                    ie.setOperator(InfixExpression.Operator.EQUALS);
                    ie.setLeftOperand(s.getAST().newSimpleName(name));
                    NumberLiteral literal = s.getAST().newNumberLiteral(((NumberLiteral)s.expressions().get(0)).toString());
                    ie.setRightOperand(literal);

                    gp.setExpression(ie);

                    listRewrite.replace((ASTNode) s.expressions().get(0),gp,null);
                }
            }
            else if(switchType.toLowerCase().contains("char")){
                if(s.expressions().get(0) instanceof CharacterLiteral && s.expressions().size() == 1){
                    if(!(((SwitchStatement)s.getParent()).getExpression() instanceof CastExpression)){
                        var cast = s.getAST().newCastExpression();
                        cast.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("Character")));

                        var exp = (Expression) ASTNode.copySubtree(s.getAST(),((SwitchStatement)s.getParent()).getExpression());
                        cast.setExpression(exp);
                        rewrite.set(s.getParent(),SwitchStatement.EXPRESSION_PROPERTY,cast,null);
                    }
                    var gp =  s.getAST().newGuardedPattern();
                    var sd = s.getAST().newSingleVariableDeclaration();
                    String name = "c"+BaseGenerator.alphanumericStringGen(rand,7);
                    sd.setName(s.getAST().newSimpleName(name));
                    sd.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("Character")));
                    TypePattern tp = s.getAST().newTypePattern();
                    tp.setPatternVariable(sd);
                    gp.setPattern(tp);

                    InfixExpression ie = s.getAST().newInfixExpression();
                    ie.setOperator(InfixExpression.Operator.EQUALS);
                    ie.setLeftOperand(s.getAST().newSimpleName(name));
                    CharacterLiteral literal = s.getAST().newCharacterLiteral();
                    literal.setCharValue(((CharacterLiteral)s.expressions().get(0)).charValue());
                    ie.setRightOperand(literal);

                    gp.setExpression(ie);

                    listRewrite.replace((ASTNode) s.expressions().get(0),gp,null);
                }
            }
            else if(switchType.toLowerCase().contains("short")){
                if(s.expressions().get(0) instanceof NumberLiteral && s.expressions().size() == 1){
                    if(!(((SwitchStatement)s.getParent()).getExpression() instanceof CastExpression)){
                        var cast = s.getAST().newCastExpression();
                        cast.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("Short")));

                        var exp = (Expression) ASTNode.copySubtree(s.getAST(),((SwitchStatement)s.getParent()).getExpression());
                        cast.setExpression(exp);
                        rewrite.set(s.getParent(),SwitchStatement.EXPRESSION_PROPERTY,cast,null);
                    }
                    var gp =  s.getAST().newGuardedPattern();
                    var sd = s.getAST().newSingleVariableDeclaration();
                    String name = "i"+BaseGenerator.alphanumericStringGen(rand,7);
                    sd.setName(s.getAST().newSimpleName(name));
                    sd.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("Short")));
                    TypePattern tp = s.getAST().newTypePattern();
                    tp.setPatternVariable(sd);
                    gp.setPattern(tp);

                    InfixExpression ie = s.getAST().newInfixExpression();
                    ie.setOperator(InfixExpression.Operator.EQUALS);
                    ie.setLeftOperand(s.getAST().newSimpleName(name));
                    NumberLiteral literal = s.getAST().newNumberLiteral(((NumberLiteral)s.expressions().get(0)).toString());
                    ie.setRightOperand(literal);

                    gp.setExpression(ie);

                    listRewrite.replace((ASTNode) s.expressions().get(0),gp,null);
                }
            }
            else if(switchType.toLowerCase().contains("byte")){
                if(s.expressions().get(0) instanceof NumberLiteral && s.expressions().size() == 1){

                    if(!(((SwitchStatement)s.getParent()).getExpression() instanceof CastExpression)){
                        var cast = s.getAST().newCastExpression();
                        cast.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("Byte")));

                        var exp = (Expression) ASTNode.copySubtree(s.getAST(),((SwitchStatement)s.getParent()).getExpression());
                        cast.setExpression(exp);
                        rewrite.set(s.getParent(),SwitchStatement.EXPRESSION_PROPERTY,cast,null);
                    }
                    var gp =  s.getAST().newGuardedPattern();
                    var sd = s.getAST().newSingleVariableDeclaration();
                    String name = "i"+BaseGenerator.alphanumericStringGen(rand,7);
                    sd.setName(s.getAST().newSimpleName(name));
                    sd.setType(s.getAST().newSimpleType(s.getAST().newSimpleName("Byte")));
                    TypePattern tp = s.getAST().newTypePattern();
                    tp.setPatternVariable(sd);
                    gp.setPattern(tp);

                    InfixExpression ie = s.getAST().newInfixExpression();
                    ie.setOperator(InfixExpression.Operator.EQUALS);
                    ie.setLeftOperand(s.getAST().newSimpleName(name));
                    NumberLiteral literal = s.getAST().newNumberLiteral(((NumberLiteral)s.expressions().get(0)).toString());
                    ie.setRightOperand(literal);

                    gp.setExpression(ie);

                    listRewrite.replace((ASTNode) s.expressions().get(0),gp,null);
                }
            }
            else{
                continue;
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
//
//            listRewrite.insertFirst(newSwitchCase, null);
//            listRewrite.insertAt(s.getAST().newExpressionStatement(methodInvocation), 1, null);
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