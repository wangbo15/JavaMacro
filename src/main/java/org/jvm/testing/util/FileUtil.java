package org.jvm.testing.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static String getUnitName(File file) {
        if (!file.getName().endsWith(".java")) {
            return file.getName();
        }
        int idx = file.getName().indexOf(".java");
        String unitName = file.getName().substring(0, idx);
        return unitName;
    }


    public static List<File> getFileList(String strPath, List<File> fileList, String postfix) {
        if (strPath == null) {
            return fileList;
        }
        File dir = new File(strPath);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    String fileName = files[i].getName();
                    if (files[i].isDirectory()) {
                        getFileList(files[i].getAbsolutePath(), fileList, postfix);
                    } else if (fileName.endsWith(postfix)) {
                        // String strFileName = files[i].getAbsolutePath();
                        // System.out.println("\t" + strFileName);
                        fileList.add(files[i]);
                    } else {
                        continue;
                    }
                }
            }
        } else {
            fileList.add(dir);
        }
        return fileList;
    }

    public static List<File> getFileList(String path) {
        File root = new File(path);
        assert root.exists() : path;
        List<File> result = new ArrayList<>();
        for (File f : root.listFiles()) {
            if (!f.isDirectory()) {
                result.add(f);
            }
        }
        return result;
    }

    public static List<String> readFileToStringList(String filePath) {
        return readFileToStringList(new File(filePath));
    }

    public static List<String> readFileToStringList(File file) {
        if (file == null || file.exists() == false) {
            return null;
        }
        List<String> result = new ArrayList<>();

        FileReader fReader = null;
        BufferedReader bReader = null;

        try {
            fReader = new FileReader(file);
            bReader = new BufferedReader(fReader);
            String line = null;
            while ((line = bReader.readLine()) != null) {
                result.add(line);
            }

        } catch (Exception e) {
            if (fReader != null) {
                try {
                    fReader.close();
                } catch (IOException e1) {
                    return null;
                }
            }
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (IOException e1) {
                    return null;
                }
            }
        }
        return result;
    }

    public static String classNameToItsSrcPath(String className) {
        String file = className.replace(".", "/");
        if (file.contains("$")) {
            int dolarIdx = file.indexOf('$');
            file = file.substring(0, dolarIdx) + ".java";
        } else {
            file += ".java";
        }
        return file;
    }

    public static File getExistingFile(String path){
        File file = new File(path);
        if (!file.exists()) {
            throw new Error("NO FILE EXITS @ " + path);
        }
        return file;
    }

    public static String readFileToString(String filePath) {
        if (filePath == null) {
            return null;
        }
        File file = getExistingFile(filePath);

        return readFileToString(file);
    }

    public static String readFileToString(File file) {
        if (file == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        FileReader fReader = null;
        BufferedReader bReader = null;
        try {
            fReader = new FileReader(file);
            bReader = new BufferedReader(fReader);
            String line = null;
            while ((line = bReader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

        } catch (Exception e) {
            if (fReader != null) {
                try {
                    fReader.close();
                } catch (IOException e1) {
                    return null;
                }
            }
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (IOException e1) {
                    return null;
                }
            }
        }
        return sb.toString();
    }

    public static boolean writeStringToFile(String file, String string, boolean append) {
        return writeStringToFile(new File(file), string, append);
    }

    public static boolean writeStringToFile(File file, String string, boolean append) {
        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append)));
            bufferedWriter.write(string);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
