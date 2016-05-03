package org.dbpedia.utils;

import org.dbpedia.utils.annotation.SpotlightAnnotator;
import org.dbpedia.utils.annotation.models.SpotlightAnnotation;
import org.dbpedia.utils.annotation.models.SpotlightResource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wojtuch on 27/04/16.
 */
public class Main {

    private static final String URL_SPOTLIGHT = "http://dbpedia:32783/rest/annotate";
//    private static final String URL_SPOTLIGHT = "http://spotlight.sztaki.hu:2222/rest/annotate";
//    private static final String URL_SPOTLIGHT = "http://spotlight.dbpedia.org/rest/annotate";

    public static void main(String[] args) throws URISyntaxException, IOException {
        String text = "Berlin is the capital of Germany.".replaceAll("\\x03", "");
        List<String> textList = Arrays.asList("Berlin is the capital of Germany", "Paris is the capital of France", "Rome is the capital of Italy");

        SparqlConnector sparqlConnector = new SparqlConnector("http://dbpedia.org/sparql");
        SpotlightAnnotator spotlightAnnotator = new SpotlightAnnotator(URL_SPOTLIGHT);

        SpotlightAnnotation annotation = spotlightAnnotator.annotate(text, 20, 0.5);
        if (annotation != null) {
            for (SpotlightResource sr : annotation.getResources()) {
                System.out.println(sr.getOffset() + ": " + sr.getUri());
                System.out.println(sparqlConnector.getCategories(sr.getUri()));
                break;
            }
        }
        else {
            System.out.println("No annotated entities.");
        }
    }
}
