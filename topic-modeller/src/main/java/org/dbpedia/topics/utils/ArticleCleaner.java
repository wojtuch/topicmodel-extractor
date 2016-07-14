package org.dbpedia.topics.utils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wlu on 14.07.16.
 */
public class ArticleCleaner {

    // regex to find links to other wiki articles
    private static Pattern linksRegex = Pattern.compile("\\[\\[([^:\\[]+?)]]");

    public static String replaceLinks(String text) {
        Matcher matcher = linksRegex.matcher(text);
        String output = new String(text);

        while (matcher.find()) {
            String matched = matcher.group(1);
            System.out.println("Found link: "+matched);
            String[] surfaceFormDBID = parseWikimediaLink(matched);
            String canonicalDbpediaId = RedirectStore.getCanonicalId(surfaceFormDBID[1]);
//            output = output.replaceAll(matched, " DBPEDIA_ID/" + canonicalDbpediaId+ " " + surfaceFormDBID[0] + " ");
        }

        return output;
    }

    private static String[] parseWikimediaLink(String matchedLink) {
        String[] split = matchedLink.replaceAll("\\[\\[", "").replaceAll("]]", "").split("\\|");

        // [[ Dbpedia Title]]
        if (split.length == 1) {
            String surfaceForm = split[0].trim();
            String dbpediaID = surfaceForm.replaceAll(" ", "_");
            return new String[]{surfaceForm, dbpediaID};
        }

        // [[Dbpedia Title | anchor]]
        String surfaceForm = split[split.length-1].trim();
        String dbpediaID = split[0].trim().replaceAll(" ", "_");
        return new String[]{surfaceForm, dbpediaID};
    }

    public static String cleanStyle(String text) {
        return text.replaceAll("\\{\\|.*\\|\\}", "");
    }

    public static String cleanCurlyBraces(String text) {
        return text.replaceAll("\\{\\{([^:\\[\\]])+\\}\\}", "");
    }

    public static String cleanCitations(String text) {
        return text.replaceAll("\\{\\{cite([^:\\{\\}])+\\}\\}", "");
    }
}
