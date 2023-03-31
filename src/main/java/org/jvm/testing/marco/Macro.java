package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.CompilationUnit;

public interface Macro {
    String apply(CompilationUnit cu, String src);
}
