package org.jvm.testing.util;

import java.io.*;

public class JavacUtil {
    public static String runCompiledClass(File file){
        Process p;
        String ret = "";
        try {
            p = Runtime.getRuntime().exec("java --enable-preview "+FileUtil.removeExtension(file.getName()), null ,file.getParentFile());
            OutputStream stdin = p.getOutputStream ();
            stdin.flush();
            stdin.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = in.readLine()) != null){
                ret += line;
            }
            BufferedReader in1 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while((line = in1.readLine()) != null){
                ret += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public  static  boolean checkCompileable(File file){

        StringBuilder stringBuilder = new StringBuilder();
        Runtime rt = Runtime.getRuntime();
        String javac = "javac";
        try {
            File errorFile = new File("error.txt");
            File outputFile = new File("out.txt");
            errorFile.delete();
            errorFile.createNewFile();
            outputFile.delete();
            // outputFile.createNewFile();
            System.out.println("before process builder for file" + file.toString());
            ProcessBuilder pb = new ProcessBuilder(javac, "--enable-preview", "--release",  "19", file.toString());
            pb.redirectError(errorFile);
            pb.redirectOutput(outputFile);
            Process p = pb.start();
            p.waitFor();
            p.destroy();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        try (BufferedReader stdInput = new BufferedReader(new FileReader("out.txt"));
             BufferedReader stdError = new BufferedReader(new FileReader("error.txt"))){

            String s;

            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }


            while ((s = stdError.readLine()) != null) {
                if(s.contains("Error:") || s.contains("error:")){
                    System.out.println(s);
                    return false;
                }
                if( s.contains("Unknown exception")){
                    System.err.println(s);
                    return false;
                }
            }
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
        System.out.println("Compilation Success!");
        return true;
    }
}
