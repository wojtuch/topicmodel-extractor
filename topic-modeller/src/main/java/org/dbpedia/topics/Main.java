package org.dbpedia.topics;

import org.dbpedia.topics.dataset.readers.Reader;
import org.dbpedia.topics.dataset.readers.impl.DBpediaAbstractsReader;
import org.dbpedia.topics.pipeline.Pipeline;
import org.dbpedia.topics.pipeline.PipelineFinisher;
import org.dbpedia.topics.pipeline.impl.*;

import java.net.URISyntaxException;

/**
 * Created by wlu on 26.05.16.
 */
public class Main {

    public static void main(String[] args) throws URISyntaxException {

        Reader reader = new DBpediaAbstractsReader("/media/data/datasets/gsoc/long_abstracts_en.ttl", 1);
        PipelineFinisher finisher = new MongoDBInsertFinisher(Constants.MONGO_SERVER, Constants.MONGO_PORT);
        Pipeline pipeline = new Pipeline(reader, finisher);

        pipeline.addTask(new FindLemmasTask());
        pipeline.addTask(new AnnotateTask(Constants.SPOTLIGHT_ENDPOINT));
        pipeline.addTask(new FindTypesTask(Constants.DBPEDIA_ENDPOINT));
        pipeline.addTask(new FindCategoriesTask(Constants.DBPEDIA_ENDPOINT));
        pipeline.addTask(new FindHypernymsTask(Constants.HYPERNYMS_ENDPOINT));

        pipeline.doWork();
    }
}
