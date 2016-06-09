package org.dbpedia.topics.pipeline;

import org.dbpedia.topics.dataset.models.Dataset;
import org.dbpedia.topics.dataset.readers.Reader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wlu on 07.06.16.
 */
public class Pipeline {
    private List<PipelineTask> pipelineTasks = new ArrayList<>();
    private Reader datasetReader;
    private PipelineFinisher pipelineFinisher;

    /**
     * Creates a Pipeline.
     * The source dataset will be provided by the Reader.readDataset() method.
     * @param datasetReader
     */
    public Pipeline(Reader datasetReader, PipelineFinisher pipelineFinisher) {
        this.datasetReader = datasetReader;
        this.pipelineFinisher = pipelineFinisher;
    }

    /**
     * Adds a task to the pipeline.
     * @param task
     * @return as specified in List.add()
     */
    public boolean addTask(PipelineTask task) {
        return this.pipelineTasks.add(task);
    }

    /**
     * Reads the dataset, passes it for sequential processing by the tasks and finishes the pipeline.
     * Eventually, the finisher
     */
    public void doWork() {
        Dataset dataset = datasetReader.readDataset();

        for (PipelineTask pipelineTask : pipelineTasks) {
            dataset = pipelineTask.start(dataset);
        }

        pipelineFinisher.finishPipeline(dataset);
    }
}
