package org.jvm.testing.marco;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;
import org.jvm.testing.util.FileUtil;
import org.jvm.testing.util.JdtUtil;

import java.io.File;

public class DuplicatedPutInsertionTest {

    @Test
    public void testApply() {
        File file = new File("/home/nightwish/workspace/compiler/testing/JavaMacro/Tests/src/main/java/org/example/CollectionsRelated.java");
        String fileUtilSrc = FileUtil.readFileToString(file);
        int idx = file.getName().indexOf(".java");
        String name = file.getName().substring(0, idx);
        final CompilationUnit cu = (CompilationUnit) JdtUtil.genASTFromSource(fileUtilSrc, name, "1.7", ASTParser.K_COMPILATION_UNIT);

        DuplicatedPutInsertion di = new DuplicatedPutInsertion();
        System.out.println(di.apply(cu, fileUtilSrc));
    }
}