package org.dbpedia.topics.pipeline;

import org.dbpedia.topics.dataset.models.Dataset;
import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.dataset.readers.Reader;
import org.dbpedia.topics.dataset.readers.StreamingReader;
import org.dbpedia.topics.pipeline.impl.MongoDBInsertFinisher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by wlu on 07.06.16.
 */
public class StreamingPipeline implements IPipeline {
    private List<PipelineTask> pipelineTasks = new ArrayList<>();
    private StreamingReader datasetReader;
    private List<PipelineFinisher> pipelineFinishers = new ArrayList<>();

    /**
     * Creates a Pipeline.
     * The source dataset will be provided by the Reader.readDataset() method.
     * @param datasetReader
     * @param pipelineFinisher
     */
    public StreamingPipeline(StreamingReader datasetReader, PipelineFinisher pipelineFinisher) {
        this.datasetReader = datasetReader;
        this.pipelineFinishers.add(pipelineFinisher);
    }

    /**
     * Creates a Pipeline.
     * The source dataset will be provided by the Reader.readDataset() method.
     * @param datasetReader
     */
    public StreamingPipeline(StreamingReader datasetReader) {
        this.datasetReader = datasetReader;
    }

    /**
     * Adds a task to the pipeline.
     * @param task
     * @return as specified in List.add()
     */
    public boolean addTask(PipelineTask task) {
        return this.pipelineTasks.add(task);
    }

    @Override
    public boolean addFinisher(PipelineFinisher finisher) {
        return this.pipelineFinishers.add(finisher);
    }

    /**
     * Reads the dataset, passes it for sequential processing by the tasks and finishes the pipeline.
     * Eventually, the finisher
     */
    public void doWork() {
        //TODO to be rewritten if needed
        System.out.println("Starting streaming pipeline.");
        Stream<Instance> dataset = datasetReader.readDataset();

        for (PipelineFinisher pipelineFinisher : pipelineFinishers) {
            // skip the pipeline if the instance is present in the database
            if (pipelineFinisher instanceof MongoDBInsertFinisher) {
                MongoDBInsertFinisher casted = (MongoDBInsertFinisher) pipelineFinisher;
                dataset = dataset.filter(instance -> !casted.recordAlreadyExists(instance));
            }

            dataset.forEach(instance -> {
                System.out.print(".");
                for (PipelineTask pipelineTask : pipelineTasks) {
                    pipelineTask.processInstance(instance);
                }
                pipelineFinisher.finishInstance(instance);
            });
        }
    }

    @Override
    public void close() {
        for (PipelineFinisher pipelineFinisher : this.pipelineFinishers) {
            pipelineFinisher.close();
        }
    }
}
