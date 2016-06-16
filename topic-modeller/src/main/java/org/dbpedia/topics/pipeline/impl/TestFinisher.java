package org.dbpedia.topics.pipeline.impl;

import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.pipeline.PipelineFinisher;

/**
 * Created by wlu on 09.06.16.
 */
public class TestFinisher extends PipelineFinisher {

    @Override
    public void finishInstance(Instance instance) {
        System.out.print(instance.getUri());
        System.out.println(instance.getLemmas());
        System.out.println(instance.getHypernyms());
        System.out.println(instance.getText());
    }

    @Override
    public void close() {}
}
