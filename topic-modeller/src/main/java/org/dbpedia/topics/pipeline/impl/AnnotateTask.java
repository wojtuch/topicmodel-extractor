package org.dbpedia.topics.pipeline.impl;

import org.dbpedia.topics.dataset.models.Dataset;
import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.pipeline.PipelineTask;
import org.dbpedia.utils.annotation.SpotlightAnnotator;
import org.dbpedia.utils.annotation.models.SpotlightAnnotation;

/**
 * Created by wlu on 09.06.16.
 */
public class AnnotateTask implements PipelineTask {

    private SpotlightAnnotator spotlightAnnotator;

    public AnnotateTask(String spotlightEndpoint) {
        spotlightAnnotator = new SpotlightAnnotator(spotlightEndpoint);
    }

    @Override
    public Dataset start(Dataset dataset) {

        for (Instance instance : dataset) {
            SpotlightAnnotation annotation = spotlightAnnotator.annotate(instance.getText(), 0, 0.5);
            instance.setSpotlightAnnotation(annotation);
        }

        return dataset;
    }
}
