package org.phstudy.ehc.utils;

import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by study on 5/20/15.
 */
public class CategoryUtils {
    public static Map<String, String> categories = Maps.newConcurrentMap();

    static {
        loadPriceFromData("/category.csv");
        loadPriceFromData("/category_pid_interpolation.csv");
    }

    public static void loadPriceFromData(String classpathLocation) {
        InputStream in = CategoryUtils.class.getResourceAsStream(classpathLocation);
        try {
            Iterator<String> it = IOUtils.lineIterator(in, Charset.defaultCharset());
            while (it.hasNext()) {
                String[] part = it.next().split(",");
                String uid = part[0];
                String category = part[1];
                categories.put(uid, category);
            }
            System.out.println("load product category: " + classpathLocation);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }

    }
}
