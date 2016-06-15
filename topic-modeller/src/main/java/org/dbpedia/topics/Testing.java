package org.dbpedia.topics;

import org.dbpedia.topics.dataset.models.impl.DBpediaAbstract;
import org.dbpedia.topics.io.MongoWrapper;

/**
 * Created by wlu on 15.06.16.
 */
public class Testing {
    public static void main(String[] args) {
        MongoWrapper mongo = new MongoWrapper(Constants.MONGO_SERVER, Constants.MONGO_PORT);

        mongo.recordExists(DBpediaAbstract.class, "http://dbpedia.org/resource/An_American_in_Paris");

        mongo.close();
    }
}
