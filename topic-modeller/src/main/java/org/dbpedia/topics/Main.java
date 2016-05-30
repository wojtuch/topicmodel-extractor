package org.dbpedia.topics;

import org.dbpedia.topics.dataset.readers.Reader;
import org.dbpedia.topics.dataset.readers.impl.DBpediaAbstractsReader;

/**
 * Created by wlu on 26.05.16.
 */
public class Main {
    public static void main(String[] args) {
        Reader reader = new DBpediaAbstractsReader("/media/data/datasets/gsoc/long_abstracts_en.ttl");
        reader.readDataset();
    }
}
