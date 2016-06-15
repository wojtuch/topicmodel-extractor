package org.dbpedia.topics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by wlu on 15.06.16.
 */
public class Utils {
    public static long getNumberOfLines(String filename) {
        try {
            return Files.lines(Paths.get(filename)).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
