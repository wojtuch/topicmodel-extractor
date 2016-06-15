package org.dbpedia.topics.pipeline;

import org.dbpedia.topics.Constants;
import org.dbpedia.topics.dataset.readers.Reader;
import org.dbpedia.topics.dataset.readers.impl.DBpediaAbstractsReader;
import org.dbpedia.topics.pipeline.impl.MongoDBInsertFinisher;

/**
 * Created by wlu on 15.06.16.
 */
public class PipelineThread implements Runnable {
    private Reader reader;
    private PipelineFinisher finisher;
    private Pipeline pipeline;

    public PipelineThread(Reader reader, PipelineFinisher finisher, Pipeline pipeline) {
        this.reader = reader;
        this.finisher = finisher;
        this.pipeline = pipeline;
    }

    @Override
    public void run() {
        pipeline.doWork();
        finisher.close();
        reader.close();
    }
}
