package org.dbpedia.topics;

import org.dbpedia.topics.dataset.models.impl.DBpediaAbstract;
import org.dbpedia.topics.dataset.readers.Reader;
import org.dbpedia.topics.dataset.readers.impl.DBpediaAbstractsReader;
import org.dbpedia.topics.dataset.readers.impl.MongoReader;
import org.dbpedia.topics.pipeline.Pipeline;
import org.dbpedia.topics.pipeline.PipelineFinisher;
import org.dbpedia.topics.pipeline.PipelineThread;
import org.dbpedia.topics.pipeline.impl.*;

import java.net.URISyntaxException;

/**
 * Created by wlu on 26.05.16.
 */
public class Main {

    public static void main(String[] args) throws URISyntaxException {

//        Reader reader = new MongoReader(DBpediaAbstract.class, Constants.MONGO_SERVER, Constants.MONGO_PORT);
//        PipelineFinisher finisher = new TestFinisher();

        Reader reader = new DBpediaAbstractsReader("/media/data/datasets/gsoc/long_abstracts_en.ttl");
        PipelineFinisher finisher = new MongoDBInsertFinisher(Constants.MONGO_SERVER, Constants.MONGO_PORT);

        Pipeline pipeline = new Pipeline(reader, finisher);

        pipeline.addTask(new FindLemmasTask());
        pipeline.addTask(new AnnotateTask(Constants.SPOTLIGHT_ENDPOINT));
        pipeline.addTask(new FindTypesTask(Constants.DBPEDIA_ENDPOINT));
        pipeline.addTask(new FindCategoriesTask(Constants.DBPEDIA_ENDPOINT));
        pipeline.addTask(new FindHypernymsTask(Constants.HYPERNYMS_ENDPOINT));

        pipeline.doWork();

        finisher.close();
        reader.close();
    }

    private void multiThreadedPipeline(int numWorkers) throws URISyntaxException {
        long numLines = Utils.getNumberOfLines("/media/data/datasets/gsoc/long_abstracts_en.ttl");
        long batchSize = numLines/numWorkers;

        for (int i = 0; i < numWorkers; i++) {
            long start = batchSize*i;
            long end = i == numWorkers-1 ? numLines : batchSize*(i+1);


            Reader reader = new DBpediaAbstractsReader("/media/data/datasets/gsoc/long_abstracts_en.ttl", start, end);
            PipelineFinisher finisher = new MongoDBInsertFinisher(Constants.MONGO_SERVER, Constants.MONGO_PORT);

            Pipeline pipeline = new Pipeline(reader, finisher);
            pipeline.addTask(new FindLemmasTask());
            pipeline.addTask(new AnnotateTask(Constants.SPOTLIGHT_ENDPOINT));
            pipeline.addTask(new FindTypesTask(Constants.DBPEDIA_ENDPOINT));
            pipeline.addTask(new FindCategoriesTask(Constants.DBPEDIA_ENDPOINT));
            pipeline.addTask(new FindHypernymsTask(Constants.HYPERNYMS_ENDPOINT));


            PipelineThread task = new PipelineThread(reader, finisher, pipeline);
            Thread thread = new Thread(task);
            thread.start();
        }
    }
}
