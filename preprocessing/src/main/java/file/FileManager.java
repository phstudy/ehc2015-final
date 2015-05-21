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
import java.util.LinkedList;
import java.util.List;
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

    public static BufferedReader fileAsReader(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        managedResources.add(reader);
        return reader;
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

    public static Iterable<String> fileAsLineIterator(String name) throws Exception {
        final BufferedReader reader = fileAsReader(name);
        managedResources.add(reader);
        final List<String> buffer = new LinkedList<String>();
        final Iterator<String> it = new Iterator<String>() {

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String next() {
                return buffer.remove(0);
            }

            @Override
            public boolean hasNext() {
                fillBuffer();
                return !buffer.isEmpty();
            }

            public void fillBuffer() {
                try {
                    String s = reader.readLine();
                    if (s != null) {
                        buffer.add(s);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        return new Iterable<String>() {

            @Override
            public Iterator<String> iterator() {
                return it;
            }
        };
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
