package org.dbpedia.topics.modelling;

/**
 * Created by wlu on 26.05.16.
 */
public interface TopicModel {
    /**
     * Predicts the coverage of mined topics for the given text.
     * @param text
     * @return
     */
    double[] predict (String text);
}
