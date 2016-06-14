package org.dbpedia.topics.pipeline.impl;

import com.mongodb.MongoClient;
import org.dbpedia.topics.dataset.models.Dataset;
import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.pipeline.PipelineFinisher;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * Created by wlu on 09.06.16.
 */
public class MongoDBInsertFinisher implements PipelineFinisher {
    private MongoClient mongoClient;
    private Morphia morphia;

    public MongoDBInsertFinisher(String server, int port) {
        mongoClient = new MongoClient(server, port);
        morphia = new Morphia();
    }

    @Override
    public void finishPipeline(Dataset dataset) {
        morphia.mapPackage("org.dbpedia.topics.dataset.models");
        Datastore datastore = morphia.createDatastore(mongoClient, "gsoc");

        for (Instance instance : dataset) {
            datastore.save(instance);
        }

        mongoClient.close();
    }
}
