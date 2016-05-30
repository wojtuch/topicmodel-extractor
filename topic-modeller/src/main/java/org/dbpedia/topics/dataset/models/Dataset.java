package org.dbpedia.topics.dataset.models;

import java.util.List;

/**
 * Representation of the document corpus that will be used for topic modelling.
 * Created by wlu on 26.05.16.
 */
public interface Dataset {
    List<Instance> getDocuments();
    void addDocument(Instance document);
}
