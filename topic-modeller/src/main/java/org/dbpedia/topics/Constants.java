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
    public static String MONGO_SERVER;
    public static int MONGO_PORT;
    public static String MONGO_DB;
    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("props.properties"));
            SPOTLIGHT_ENDPOINT = properties.getProperty("spotlight_endpoint", "http://spotlight.sztaki.hu:2222/rest/annotate");
            DBPEDIA_ENDPOINT = properties.getProperty("dbpedia_sparql_endpoint", "http://dbpedia.org/sparql");
            HYPERNYMS_ENDPOINT = properties.getProperty("hypernyms_sparql_endpoint");
            MONGO_SERVER = properties.getProperty("mongo_server", "localhost");
            MONGO_PORT = Integer.parseInt(properties.getProperty("mongo_port", "27017"));
            MONGO_DB = properties.getProperty("mongo_dbname", "gsoc");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static final String FEATURE_DESCRIPTOR_WORDS = "w";
    public static final String FEATURE_DESCRIPTOR_ENTITIES = "e";
    public static final String FEATURE_DESCRIPTOR_TYPES = "t";
    public static final String FEATURE_DESCRIPTOR_CATEGORIES = "c";
    public static final String FEATURE_DESCRIPTOR_HYPERNYMS = "h";

    public static final String FEATURE_PREFIX_ENTITIES = "E:";
    public static final String FEATURE_PREFIX_TYPES = "rdfT:";
    public static final String FEATURE_PREFIX_CATEGORIES = "dcS:";
    public static final String FEATURE_PREFIX_HYPERNYMS = "H:";
}
