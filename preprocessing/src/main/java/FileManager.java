import java.io.File;

public class FileManager {

    static String baseDir = "/Users/study/Desktop/EHC";
    static {
        String preferedBaseDir = System.getProperty("EHC_FINAL_DATASET_DIR");
        if (preferedBaseDir != null && new File(preferedBaseDir).exists() && new File(preferedBaseDir).isDirectory()) {
            baseDir = new File(preferedBaseDir).getAbsolutePath();
            System.err.println("reset base-dir as " + baseDir);
        }
    }

    static File file(String name) {
        return new File(baseDir, name);
    }

}
