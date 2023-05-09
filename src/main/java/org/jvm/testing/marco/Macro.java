package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Random;

public interface Macro {
    String apply(CompilationUnit cu, String src, Random rand);

    boolean isMacroApplicable(String src);

}
