package org.jvm.testing.main;

import org.apache.felix.gogo.runtime.Parser;
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

    public static boolean jdkTestMode = true;

    public static boolean modifyOriginalSource = false;
    public static String outputFoler = "/modified";
    public static String jdkTestFolder = "/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/";

    public static void mainsdf(String[] args) {
        ASTNode e = JdtUtil.genASTFromSource("new Int[]{1,2,3}", "expression", JavaCore.VERSION_20, ASTParser.K_EXPRESSION);
        System.out.println("class:"+e.getClass());
        ArrayCreation ac = (ArrayCreation) e;
        System.out.println(ac.getInitializer().getClass());
        System.out.println(((ArrayInitializer)ac.getInitializer()).expressions().get(0));
        System.out.println(ac.getType());
        System.out.println(ac.getType().getElementType().getClass());
        System.out.println(ac.getAST());
    }

    public static void main(String[] args) {

        System.out.println("Strarting Macros!!!");
        Random rand = new Random();

//        String testRoot = "/home/nightwish/workspace/compiler/jvm/jdk/test/hotspot/jtreg";
//        String testRoot = "./Tests/src/main/java";
       String testRoot = "/home/admin1/Desktop/hello10/HelloWorld3.java";
        //String testRoot = "/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/BadOptimization/Switch1.java";
        //String testRoot = "/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/BadOptimization";
        //       String testRoot = jdkTestFolder;

        //String testRoot = "/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac";
      //  String testRoot = "/home/admin1/Downloads/jdk-jdk-20-34/test";
        //String testRoot = "/home/admin1/Desktop/hello10";

        if(jdkTestMode){
            modifyOriginalSource = true;
            testRoot = "/home/admin1/Downloads/jdk-jdk-20-34/test";//langtools/tools/javac";
        }
        List<File> tests = new ArrayList<>(1000);

        //FileUtil.createDir(testRoot+outputFoler);

        long start = System.currentTimeMillis();
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
        long elapsedTimeMillis = System.currentTimeMillis()-start;
        float elapsedTimeSec = elapsedTimeMillis/1000F;
        System.out.println("applyMacro time" + elapsedTimeSec);

//        outputFiles.add(new File("/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/BadOptimization/modified/Switch1.java"));
//        outputFiles.add(new File("/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/BadOptimization/modified/Switch2.java"));

        //applyMacro(new File("/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/BadOptimization/Switch1.java"),rand);
        //String res = TestUtil.runJDKTests("/home/admin1/Downloads/jdk-jdk-20-34/test/langtools/tools/javac/BadOptimization/modified/Switch1.java");

        System.out.println("Applicable Test Number: "+ outputFiles.size());

        if(jdkTestMode) {
            start = System.currentTimeMillis();
            String res = TestUtil.runJDKTests(outputFiles);
            System.out.println(res);
            elapsedTimeMillis = System.currentTimeMillis() - start;
            elapsedTimeSec = elapsedTimeMillis / 1000F;
            System.out.println("testing time" + elapsedTimeSec);
        }




//        if(!modifyOriginalSource) {
//            FileUtil.clearTestFiles(outputFiles);
//        }

    }


    private static List<Macro> activatedMacros() {
        List<Macro> macros = new ArrayList<>();
 //       macros.add(new NumberUpscaler());
      //  macros.add(new ExchangeOrderForCommutatives());
 //       macros.add(new SwitchMethodOrder());
 //       macros.add(new DuplicatedPutInsertion());
        //macros.add(new AddSwitchCase());
        //macros.add(new AddSwitchLabels());
 //       macros.add(new AddSwitchCaseGuard());
 //        macros.add(new AddRecords());
 //       macros.add(new ReplaceSwitchCaseRepresentation());
 //         macros.add(new AddVarsToRecords());
  //       macros.add(new AddMethodsToRecords());
 //       macros.add(new AddModifiersToRecords());
 //       macros.add(new AddVectorApiRepresentation());
 //      macros.add(new ReplaceLongWithVectorApiRepresentation());
 //       macros.add(new ReplaceIntWithVectorApiRepresentation());
 //     macros.add(new ReplaceFloatWithVectorApiRepresentation());
 //       macros.add(new ReplaceDoubleWithVectorApiRepresentation());
  //      macros.add(new ReplaceShortWithVectorApiRepresentation());
   //     macros.add(new ReplaceByteWithVectorApiRepresentation());
        macros.add(new AddNullSwitchCase());
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
