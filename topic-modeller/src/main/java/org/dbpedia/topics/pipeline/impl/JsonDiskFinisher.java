package org.dbpedia.topics.pipeline.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.DuplicateKeyException;
import org.bson.BSONException;
import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.io.MongoWrapper;
import org.dbpedia.topics.pipeline.PipelineFinisher;
import org.mongodb.morphia.Datastore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by wlu on 09.06.16.
 */
public class JsonDiskFinisher extends PipelineFinisher {
    private String outputDir;
    private int counter = 0;

    public JsonDiskFinisher(String outputDir) {
        this.outputDir = outputDir;
        new File(outputDir).mkdirs();
    }

    @Override
    public void finishInstance(Instance instance) {
        if (instance.getSpotlightAnnotation() == null) {
            System.err.println("Document not annotated....");
            return;
        }

        if (instance.getSpotlightAnnotation().isEmpty()) {
            System.err.println("Annotation empty....");
            return;
        }

        try (Writer writer = new FileWriter(new File(outputDir, (++this.counter)+".json"))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
    }
}
