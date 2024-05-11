package serviceexperimantal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

public class Experimental {
    public static String out = "src/main/java/data/output/GA/";
    public static String raw = "src/main/java/data/output/GAraw";
    public static void main(String[] args) {
        copy();
    }

    public static void copy() {
        createNewDicGA();
        // Đường dẫn tới thư mục chứa các thư mục con
        String rootPath = raw;
        // Duyệt qua tất cả các thư mục con
        File rootDir = new File(rootPath);
        File[] subDirs = rootDir.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                classifyFilesInDirectory(subDir);
            }
        }
    }

    public static void createNewDicGA() {
        String rootPath = raw+"/0.1";
        String outPath = out;
        File rootDir = new File(rootPath);
        File outDir = new File(outPath);
        try {
            FileUtils.deleteDirectory(outDir);
        } catch (Exception e) {

        }
        outDir.mkdir();
        File[] files = rootDir.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName().substring(0, file.getName().indexOf("_2P_0.json")).concat("requests");
                String path = outPath + name;
                File dic = new File(path);
                if (dic.exists()) {
                    dic.delete();
                }
                dic.mkdir();
            }
        }
    }

    public static void classifyFilesInDirectory(File directory) {
        // Lấy tất cả các tệp trong thư mục
        String outPath = out;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName().substring(0, file.getName().indexOf("_2P_0.json")).concat("requests");
                String path = outPath + name;
                Path sourcePath = Paths.get(file.getPath());
                Path targetPath = Paths.get(path, name + "_alpha_".concat(directory.getName()) + ".json");
                try {
                    Files.copy(sourcePath, targetPath);
                    System.out.println("Sao chép tệp thành công.");
                } catch (IOException e) {
                    System.out.println("Không thể sao chép tệp: " + e.getMessage());
                }
            }
        }
    }
}
