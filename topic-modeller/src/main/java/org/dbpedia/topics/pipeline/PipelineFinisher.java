package org.dbpedia.topics.pipeline;

import org.dbpedia.topics.dataset.models.Dataset;

/**
 * An interface for the last step of the pipeline.
 * Created by wlu on 07.06.16.
 */
public interface PipelineFinisher {
    /**
     * Performs the last step of the pipeline.
     * @param dataset
     */
    void finishPipeline(Dataset dataset);
}
