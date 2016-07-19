package org.dbpedia.topics.inference.service.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wlu on 19.07.16.
 */
public class PredictionResponse implements Serializable {
    private List<Prediction> predictions = new ArrayList<>();

    public PredictionResponse() {
    }

    public boolean addPrediction(Prediction prediction) {
        return predictions.add(prediction);
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }
}
