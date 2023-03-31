package org.jvm.testing.main;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jvm.testing.marco.*;
import org.jvm.testing.util.FileUtil;
import org.jvm.testing.util.JdtUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Main {

    public static void main(String[] args) {

//        String testRoot = "/home/nightwish/workspace/compiler/jvm/jdk/test/hotspot/jtreg";
        String testRoot = "./Tests/src/main/java";
        List<File> tests = new ArrayList<>(1000);
        FileUtil.getFileList(testRoot, tests, ".java");

        int time = 0;
        for (File file : tests) {
            applyMacro(file);
            time++;
            if (time > 500)
                break;
        }

    }

    private static List<Macro> activatedMacros() {
        List<Macro> macros = new ArrayList<>();
//        macros.add(new NumberUpscaler());
        macros.add(new ExchangeOrderForCommutatives());
        macros.add(new SwitchMethodOrder());
        macros.add(new DuplicatedPutInsertion());
        return macros;
    }

    private static void applyMacro(File file) {
//        try {
            System.out.println(">>>> Processing " + file.getAbsolutePath());
            String oriSrc = FileUtil.readFileToString(file);
            String unitName = FileUtil.getUnitName(file);

            List<Macro> macros = activatedMacros();

            String currSrc = oriSrc;
            for (Macro macro: macros) {
                CompilationUnit cu = (CompilationUnit) JdtUtil.genASTFromSource(currSrc, unitName, "1.8", ASTParser.K_COMPILATION_UNIT);
                currSrc = macro.apply(cu, currSrc);
            }

            FileUtil.writeStringToFile(file, currSrc, false);
            //System.out.println(currSrc);
            System.out.println(">>>> END");

//       } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

}
