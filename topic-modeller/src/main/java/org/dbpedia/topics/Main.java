package org.dbpedia.topics;

import org.dbpedia.topics.dataset.readers.impl.DBpediaAbstractsTestReader;
import org.dbpedia.topics.pipeline.Pipeline;
import org.dbpedia.topics.pipeline.impl.*;

import java.net.URISyntaxException;

/**
 * Created by wlu on 26.05.16.
 */
public class Main {

    public static void main(String[] args) throws URISyntaxException {

        Pipeline pipeline = new Pipeline(new DBpediaAbstractsTestReader("/media/data/datasets/gsoc/long_abstracts_en.ttl", 15), new TestFinisher());

        pipeline.addTask(new AnnotateTask(Constants.SPOTLIGHT_ENDPOINT));
        pipeline.addTask(new FindTypesTask(Constants.DBPEDIA_ENDPOINT));
        pipeline.addTask(new FindCategoriesTask(Constants.DBPEDIA_ENDPOINT));
        pipeline.addTask(new FindHypernymsTask(Constants.HYPERNYMS_ENDPOINT));

        pipeline.doWork();
    }
}
