package org.dbpedia.topics.dataset.readers;

import org.dbpedia.topics.dataset.models.Dataset;

import java.io.IOException;

/**
 * Interface for reading the data that will be used for topic modelling.
 * Created by wlu on 26.05.16.
 */
public interface Reader {

    /**
     * Reads and returns the dataset.
     * @return
     */
    Dataset readDataset();
}
