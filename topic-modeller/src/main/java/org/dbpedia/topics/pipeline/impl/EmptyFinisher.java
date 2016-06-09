package org.dbpedia.topics.pipeline.impl;

import org.dbpedia.topics.dataset.models.Dataset;
import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.pipeline.PipelineFinisher;

/**
 * Created by wlu on 09.06.16.
 */
public class EmptyFinisher implements PipelineFinisher {
    @Override
    public void finishPipeline(Dataset dataset) {}
}
