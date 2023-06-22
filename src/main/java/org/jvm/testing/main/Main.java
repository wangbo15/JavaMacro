package org.jvm.testing.main;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.jvm.testing.marco.*;
import org.jvm.testing.util.FileUtil;
import org.jvm.testing.util.JdtUtil;
import org.jvm.testing.util.TestUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Main {

    public static boolean modifyOriginalSource = true;
    public static String outputFoler = "/modified";
    public static String jdkTestFolder = "/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/";



    public static void main(String[] args) {
        System.out.println("Strarting Macros!!!");
        Random rand = new Random();

//        String testRoot = "/home/nightwish/workspace/compiler/jvm/jdk/test/hotspot/jtreg";
//        String testRoot = "./Tests/src/main/java";
//        String testRoot = "/home/admin1/Desktop/hello10/HelloWorld4.java";
        //String testRoot = "/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/BadOptimization/Switch1.java";
        String testRoot = "/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/BadOptimization";
        //       String testRoot = jdkTestFolder;

        //String testRoot = "/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac";
        //String testRoot = "/home/admin1/Desktop/hello10";
        List<File> tests = new ArrayList<>(1000);

        //FileUtil.createDir(testRoot+outputFoler);

        FileUtil.getFileList(testRoot, tests, ".java");
        ArrayList<File> outputFiles = new ArrayList<>();
        int time = 0;
        for (File file : tests) {
            String src = FileUtil.readFileToString(file);

            if(!applyMacro(file,rand,outputFiles)){
                continue;
            }
            time++;
            if (time > 500)
                break;
        }


//        outputFiles.add(new File("/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/BadOptimization/modified/Switch1.java"));
//        outputFiles.add(new File("/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/BadOptimization/modified/Switch2.java"));

        //applyMacro(new File("/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/BadOptimization/Switch1.java"),rand);
        //String res = TestUtil.runJDKTests("/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/BadOptimization/modified/Switch1.java");


        String res = TestUtil.runJDKTests(outputFiles);
        System.out.println(res);

//        if(!modifyOriginalSource) {
//            FileUtil.clearTestFiles(outputFiles);
//        }

    }


    private static List<Macro> activatedMacros() {
        List<Macro> macros = new ArrayList<>();
//        macros.add(new NumberUpscaler());
//        macros.add(new ExchangeOrderForCommutatives());
//        macros.add(new SwitchMethodOrder());
//        macros.add(new DuplicatedPutInsertion());
//        macros.add(new AddSwitchCase());
        // macros.add(new AddSwitchLabels());
        // macros.add(new AddSwitchCaseGuard());
        // macros.add(new AddRecords());
        // macros.add(new ReplaceSwitchCaseRepresentation());
        // macros.add(new AddVarsToRecords());
        // macros.add(new AddMethodsToRecords());
        macros.add(new AddModifiersToRecords());
        return macros;
    }

    private static boolean applyMacro(File file, Random rand, ArrayList<File> outputFiles) {
//        try {
        boolean anyMacroApplicable = false;
        System.out.println(">>>> Processing " + file.getAbsolutePath());
        String oriSrc = FileUtil.readFileToString(file);
        String unitName = FileUtil.getUnitName(file);

        List<Macro> macros = activatedMacros();

        String currSrc = oriSrc;
        for (Macro macro: macros) {
            if(!macro.isMacroApplicable(oriSrc)){
                System.out.println("Macro not applicable");
                continue;
            }
            else
            {
                anyMacroApplicable = true;
            }
            CompilationUnit cu;
            try {
                cu = (CompilationUnit) JdtUtil.genASTFromSource(currSrc, unitName, JavaCore.VERSION_20, ASTParser.K_COMPILATION_UNIT);
            }
            catch (Exception e){
                System.err.println(e);
                anyMacroApplicable = false;
                break;
            }
            System.out.println("CU Length"+cu.getLength());
            for (var p:cu.getProblems()) {
                System.out.println(p.getMessage() + " at Line "+p.getSourceLineNumber());
            }
            currSrc = macro.apply(cu, currSrc,rand);
            System.out.println(currSrc);
        }
        if(anyMacroApplicable) {
            String outputPath = file.getAbsolutePath();
            if (!modifyOriginalSource) {
                outputPath = file.getParent() + outputFoler + "/" + file.getName();
            }
            if(!oriSrc.equals(currSrc)) {
                FileUtil.writeStringToFile(new File(outputPath), currSrc, false);
                outputFiles.add(new File(outputPath));
            }
            //System.out.println(currSrc);
            System.out.println(">>>> END");
        }
        return anyMacroApplicable;

//       } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

}
