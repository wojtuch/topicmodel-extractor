package org.dbpedia.topics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static Map<String, List<String>> readSubjectObjectMappings(String filename, String parseRegex) {
        Map<String, List<String>> result = new HashMap<>();

        try {
            Pattern pattern = Pattern.compile(parseRegex);
            Files.lines(Paths.get(filename))
                    .filter(line -> !line.startsWith("#"))
                    .forEach(line -> {
                        Matcher m = pattern.matcher(line);
                        if (m.find()) {
                            String subject = m.group(1);
                            String object = m.group(2);
                            result.putIfAbsent(subject, new ArrayList<>());
                            result.get(subject).add(object);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
