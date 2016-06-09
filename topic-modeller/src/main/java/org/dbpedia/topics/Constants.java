package org.dbpedia.topics;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by wlu on 09.06.16.
 */
public class Constants {

    public static String SPOTLIGHT_ENDPOINT;
    public static String DBPEDIA_ENDPOINT;
    public static String HYPERNYMS_ENDPOINT;
    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("props.properties"));
            SPOTLIGHT_ENDPOINT = properties.getProperty("spotlight_endpoint", "http://spotlight.sztaki.hu:2222/rest/annotate");
            DBPEDIA_ENDPOINT = properties.getProperty("dbpedia_sparql_endpoint", "http://dbpedia.org/sparql");
            HYPERNYMS_ENDPOINT = properties.getProperty("hypernyms_sparql_endpoint");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
