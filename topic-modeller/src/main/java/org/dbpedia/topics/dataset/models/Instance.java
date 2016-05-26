package org.dbpedia.topics.dataset.models;

/**
 * Representation of the single document used for topic modelling.
 * Created by wlu on 26.05.16.
 */
public interface Instance {

    /**
     * Returns the URI of this document.
     * @return
     */
    String getUri();

    /**
     * Returns the text content of this document.
     * @return
     */
    String getText();
}
