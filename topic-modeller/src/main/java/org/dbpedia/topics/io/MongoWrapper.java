package org.dbpedia.topics.io;

import com.mongodb.MongoClient;
import org.dbpedia.topics.Constants;
import org.dbpedia.topics.dataset.models.impl.DBpediaAbstract;
import org.dbpedia.topics.dataset.models.impl.WikipediaArticle;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * Created by wlu on 14.06.16.
 */
public class MongoWrapper {
    private MongoClient mongoClient;
    private Morphia morphia;
    private Datastore datastore;

    public MongoWrapper(String server, int port) {
        mongoClient = new MongoClient(server, port);
        morphia = new Morphia();

        morphia.map(DBpediaAbstract.class);
        morphia.map(WikipediaArticle.class);

        datastore = morphia.createDatastore(mongoClient, Constants.MONGO_DB);
        datastore.ensureIndexes();
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public void close() {
        this.mongoClient.close();
    }
}
