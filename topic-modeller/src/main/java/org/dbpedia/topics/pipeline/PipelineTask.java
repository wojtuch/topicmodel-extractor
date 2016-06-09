package org.dbpedia.topics.pipeline;

import org.dbpedia.topics.dataset.models.Dataset;

/**
 * An interface for a single processing step of the data within the processing pipeline.
 * Created by wlu on 07.06.16.
 */
public interface PipelineTask {
    /**
     * Processes the dataset.
     * @param dataset
     * @return Returns the processed dataset.
     */
    Dataset start(Dataset dataset);
}
