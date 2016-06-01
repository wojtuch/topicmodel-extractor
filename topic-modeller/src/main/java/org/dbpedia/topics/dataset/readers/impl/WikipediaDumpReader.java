package org.dbpedia.topics.dataset.readers.impl;

import org.dbpedia.topics.dataset.models.Dataset;
import org.dbpedia.topics.dataset.models.impl.WikipediaCorpus;
import org.dbpedia.topics.dataset.readers.Reader;

/**
 * Created by wlu on 29.05.16.
 */
public class WikipediaDumpReader implements Reader {

    @Override
    public Dataset readDataset() {
        Dataset dataset = new WikipediaCorpus();

        return dataset;
    }
}
