package org.dbpedia.topics.inference;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by wojlukas on 7/15/16.
 */
public class Config {
    public static String SERVER_BASE_URI;
    public static String TOPIC_MODEL_FILE;
    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("props.properties"));
            SERVER_BASE_URI = properties.getProperty("server_base_uri", "http://0.0.0.0:8182/inference-service/");
            TOPIC_MODEL_FILE = properties.getProperty("topic_model_file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
