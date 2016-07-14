package org.dbpedia.topics.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wlu on 14.07.16.
 */
public class RedirectStore {
    private static Map<String, String> redirects = new HashMap<>();

    public static String getCanonicalId(String id) {
        return redirects.getOrDefault(id, id);
    }
}
