package org.dbpedia.topics.dataset.models.impl;

import org.dbpedia.topics.dataset.models.Dataset;
import org.dbpedia.topics.dataset.models.Instance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wlu on 26.05.16.
 */
public class DBpediaAbstractsDataset implements Dataset {

    private List<Instance> documents = new ArrayList<>();

    public DBpediaAbstractsDataset() {
    }

    @Override
    public List<Instance> getDocuments() {
        return documents;
    }

    @Override
    public void addDocument(Instance document) {
        documents.add(document);
    }
}
