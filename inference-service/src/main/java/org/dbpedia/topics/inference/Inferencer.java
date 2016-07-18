package org.dbpedia.topics.inference;

import org.dbpedia.topics.modelling.LdaModel;

import java.io.IOException;

/**
 * Created by wojlukas on 7/18/16.
 */
public class Inferencer {
    private static LdaModel ldaModel = new LdaModel("e", "h");

    public static void loadFile(String file) throws IOException, ClassNotFoundException {
        ldaModel.readFromFile(file);
    }

    public static double[] predictTopicCoverage(String text) {
        double[] prediction = ldaModel.predict(text);
        return prediction;
    }
}
