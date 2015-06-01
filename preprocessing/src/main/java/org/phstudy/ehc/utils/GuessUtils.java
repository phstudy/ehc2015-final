package org.phstudy.ehc.utils;

import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by study on 5/20/15.
 */
public class GuessUtils {
    public static Set<String> guess = Sets.newConcurrentHashSet();

    static {
        loadPriceFromData("/TEST_ALREADY_GUESS");
    }

    public static void loadPriceFromData(String classpathLocation) {
        InputStream in = GuessUtils.class.getResourceAsStream(classpathLocation);
        try {
            Iterator<String> it = IOUtils.lineIterator(in, Charset.defaultCharset());
            while (it.hasNext()) {
                String uid = it.next();
                guess.add(uid);
            }
            System.out.println("load guessed product: " + classpathLocation);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }

    }
}
