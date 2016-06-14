package org.dbpedia.topics.pipeline.impl;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import org.dbpedia.topics.Constants;
import org.dbpedia.topics.dataset.models.Dataset;
import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.dataset.models.impl.DBpediaAbstract;
import org.dbpedia.topics.dataset.models.impl.WikipediaArticle;
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
        morphia.map(DBpediaAbstract.class);
        morphia.map(WikipediaArticle.class);

        Datastore datastore = morphia.createDatastore(mongoClient, Constants.MONGO_DB);
        datastore.ensureIndexes();

        for (Instance instance : dataset) {
            try {
                datastore.save(instance);
            }
            catch (DuplicateKeyException e) {
                System.out.println("Duplicate entry: " + instance.getUri());
            }
        }

        mongoClient.close();
    }
}
