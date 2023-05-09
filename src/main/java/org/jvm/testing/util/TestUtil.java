package org.jvm.testing.util;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestUtil {

    public static String JDK_PATH = "/home/admin1/Downloads/jdk-jdk-20-34";


    public static String runJDKTests(File testJavaFile){
        return runJDKTests(Arrays.asList(testJavaFile));
    }

    public static String runJDKTests(List<File> testJavaFiles){
        if(testJavaFiles.isEmpty()){
            return "No applicable tests for the selected macros.";
        }

        String ret = "";
        String tests ="";
        for (File f: testJavaFiles) {
            tests += "jtreg:"+ f.getAbsolutePath() +  " ";
        }
        if(tests.length() > 0) {
            tests = tests.substring(0, tests.length() - 1);
        }
        System.out.println(tests);



        //Runtime rt = Runtime.getRuntime();
        try {
//            ProcessBuilder pb = new ProcessBuilder("make", "test TEST=\""+tests+"\"");
//            pb.directory(new File(JDK_PATH));
//            Process p = pb.start();
//
//
//
//            System.out.println(new String(p.getInputStream().readAllBytes()));
//
//
//            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//
//            String s;
//            while ((s = stdInput.readLine()) != null) {
//                System.out.println(s);
//                ret += s+"\n";
//            }
//            while ((s = stdError.readLine()) != null) {
//                if(s.contains("Error:") || s.contains("error:")){
//                    System.out.println(s);
//                }
//                if( s.contains("Unknown exception")){
//                    System.err.println(s);
//                }
//            }
//
//
//
//            p.waitFor();

            List<String> scriptArgs = new ArrayList<>();
            scriptArgs.add("make");
            scriptArgs.add("test");
            scriptArgs.add("'TEST=\""+tests+"\"'");
            //scriptArgs.add("\""+tests+"\"");

            ProcessBuilder builder = new ProcessBuilder(scriptArgs);//"make", "test", "TEST=\""+tests+"\"");
            builder.directory(new File(JDK_PATH));
            builder.redirectError(ProcessBuilder.Redirect.INHERIT);
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process p = builder.start();
            p.waitFor();

            System.out.println(createCommandLine(2,JDK_PATH,(String[])  Arrays.copyOf(scriptArgs.toArray(), scriptArgs.size(), String[].class)));
            System.out.println(p.info());


            //p.destroy();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }




        return ret;
    }

    private static final int VERIFICATION_CMD_BAT = 0;
    private static final int VERIFICATION_WIN32 = 1;
    private static final int VERIFICATION_LEGACY = 2;
    private static final char ESCAPE_VERIFICATION[][] = {
            // We guarantee the only command file execution for implicit [cmd.exe] run.
            //    http://technet.microsoft.com/en-us/library/bb490954.aspx
            {' ', '\t', '<', '>', '&', '|', '^'},

            {' ', '\t', '<', '>'},
            {' ', '\t'}
    };

    private static boolean isQuoted(boolean noQuotesInside, String arg,
                                    String errorMessage) {
        int lastPos = arg.length() - 1;
        if (lastPos >=1 && arg.charAt(0) == '"' && arg.charAt(lastPos) == '"') {
            // The argument has already been quoted.
            if (noQuotesInside) {
                if (arg.indexOf('"', 1) != lastPos) {
                    // There is ["] inside.
                    throw new IllegalArgumentException(errorMessage);
                }
            }
            return true;
        }
        if (noQuotesInside) {
            if (arg.indexOf('"') >= 0) {
                // There is ["] inside.
                throw new IllegalArgumentException(errorMessage);
            }
        }
        return false;
    }
    private static boolean needsEscaping(int verificationType, String arg) {
        // Switch off MS heuristic for internal ["].
        // Please, use the explicit [cmd.exe] call
        // if you need the internal ["].
        //    Example: "cmd.exe", "/C", "Extended_MS_Syntax"

        // For [.exe] or [.com] file the unpaired/internal ["]
        // in the argument is not a problem.
        boolean argIsQuoted = isQuoted(
                (verificationType == VERIFICATION_CMD_BAT),
                arg, "Argument has embedded quote, use the explicit CMD.EXE call.");

        if (!argIsQuoted) {
            char testEscape[] = ESCAPE_VERIFICATION[verificationType];
            for (int i = 0; i < testEscape.length; ++i) {
                if (arg.indexOf(testEscape[i]) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }
    private static String createCommandLine(int verificationType,
                                            final String executablePath,
                                            final String cmd[])
    {
        StringBuilder cmdbuf = new StringBuilder(80);

        cmdbuf.append(executablePath);

        for (int i = 1; i < cmd.length; ++i) {
            cmdbuf.append(' ');
            String s = cmd[i];
            if (needsEscaping(verificationType, s)) {
                cmdbuf.append('"').append(s);

                // The code protects the [java.exe] and console command line
                // parser, that interprets the [\"] combination as an escape
                // sequence for the ["] char.
                //     http://msdn.microsoft.com/en-us/library/17w5ykft.aspx
                //
                // If the argument is an FS path, doubling of the tail [\]
                // char is not a problem for non-console applications.
                //
                // The [\"] sequence is not an escape sequence for the [cmd.exe]
                // command line parser. The case of the [""] tail escape
                // sequence could not be realized due to the argument validation
                // procedure.
                if ((verificationType != VERIFICATION_CMD_BAT) && s.endsWith("\\")) {
                    cmdbuf.append('\\');
                }
                cmdbuf.append('"');
            } else {
                cmdbuf.append(s);
            }
        }
        return cmdbuf.toString();
    }

    public static String runJDKTestsOld(List<File> testJavaFiles){
        if(testJavaFiles.isEmpty()){
            return "No applicable tests for the selected macros.";
        }
        Process p;
        String ret = "";
        String tests ="";
        for (File f: testJavaFiles) {
            tests += "jtreg:"+ f.getAbsolutePath() +  "\\ ";
        }
        if(tests.length() > 0) {
            tests = tests.substring(0, tests.length() - 1);
        }
        System.out.println(tests);
        try {
            //jtreg:test/jdk/jdk/internal/vm/Continuation/HumongousStack.java
            String[] commands = new String[]{"make", "test", "TEST=\""+tests+"\""};
           // System.out.println(command);
            p = Runtime.getRuntime().exec(commands,null ,new File(JDK_PATH));
            OutputStream stdin = p.getOutputStream ();
            stdin.flush();
            stdin.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = in.readLine()) != null){
                ret += line +"\n";
            }
            BufferedReader in1 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while((line = in1.readLine()) != null){
                ret += line +"\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
