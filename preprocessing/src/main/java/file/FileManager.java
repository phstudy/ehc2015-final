package file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FileManager {

    static String baseDir = "/Users/study/Desktop/EHC";

    static Set<Closeable> managedResources = Collections.synchronizedSet(new HashSet<Closeable>());

    static {
        String preferedBaseDir = System.getProperty("EHC_FINAL_DATASET_DIR");
        if (preferedBaseDir != null && new File(preferedBaseDir).exists() && new File(preferedBaseDir).isDirectory()) {
            baseDir = new File(preferedBaseDir).getAbsolutePath();
            System.err.println("reset base-dir as " + baseDir);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                FileManager.closeManagedResources();
            }
        });
    }

    public static File file(String name) {
        return new File(baseDir, name);
    }

    public static BufferedReader fileAsReader(String name) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file(name)));
        managedResources.add(reader);
        return reader;
    }

    public static BufferedWriter fileAsWriter(String name) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file(name)));
        managedResources.add(writer);
        return writer;
    }

    static void closeManagedResources() {
        Iterator<Closeable> it = managedResources.iterator();
        while (it.hasNext()) {
            Closeable c = it.next();
            try {
                System.err.println("close " + c);
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            it.remove();
        }
    }

}
