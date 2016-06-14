package org.dbpedia.topics.dataset.readers.impl;

import org.dbpedia.topics.dataset.models.Dataset;
import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.dataset.readers.Reader;
import org.dbpedia.topics.io.MongoWrapper;
import org.mongodb.morphia.Datastore;

import java.util.List;

/**
 * Created by wlu on 29.05.16.
 */
public class MongoReader<T extends Instance> implements Reader {
    private Class<T> clazz;
    private MongoWrapper mongo;

    public MongoReader(Class<T> clazz, String server, int port) {
        mongo = new MongoWrapper(server, port);
        this.clazz = clazz;
    }

    @Override
    public Dataset readDataset() {
        Datastore datastore = mongo.getDatastore();

        Dataset dataset = new Dataset();

        List<T> result = datastore.createQuery(clazz).asList();
        result.forEach(r -> dataset.addDocument(r));
        mongo.close();

        return dataset;
    }
}
