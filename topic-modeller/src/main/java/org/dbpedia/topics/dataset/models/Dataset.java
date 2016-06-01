package org.dbpedia.topics.dataset.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of the document corpus that will be used for topic modelling.
 * Created by wlu on 26.05.16.
 */
public abstract class Dataset {

    private List<Instance> documents = new ArrayList<>();

    public List<Instance> getDocuments() {
        return documents;
    }

    public void addDocument(Instance document) {
        documents.add(document);
    }
}
