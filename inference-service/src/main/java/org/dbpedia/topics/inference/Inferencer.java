package org.dbpedia.topics.inference;

import org.dbpedia.topics.inference.service.models.EmptyFinisher;
import org.dbpedia.topics.inference.service.models.InputToDataset;
import org.dbpedia.topics.modelling.LDAInputGenerator;
import org.dbpedia.topics.modelling.LdaModel;
import org.dbpedia.topics.pipeline.Pipeline;
import org.dbpedia.topics.pipeline.impl.FindCategoriesInMemoryTask;
import org.dbpedia.topics.pipeline.impl.FindHypernymsInMemoryTask;
import org.dbpedia.topics.pipeline.impl.FindLemmasTask;
import org.dbpedia.topics.pipeline.impl.FindTypesInMemoryTask;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by wojlukas on 7/18/16.
 */
public class Inferencer {

    private static Inferencer instance = null;

    public static Inferencer getInferencer(String[] features) {
        if (instance == null) {
            instance = new Inferencer(features);
        }

        return instance;
    }

    public Inferencer(String[] features) {
        ldaModel = new LdaModel(features);
        inputGenerator = new LDAInputGenerator(features);
        findHypernymsTask = new FindHypernymsInMemoryTask(Config.HYPERNYMS_TRIPLE_FILE);
//        findTypesTask = new FindTypesInMemoryTask(Config.TYPES_TRIPLE_FILE);
//        findCategories = new FindCategoriesInMemoryTask(Config.CATEGORIES_TRIPLE_FILE);
    }

    private LdaModel ldaModel;
    private LDAInputGenerator inputGenerator;
    private FindHypernymsInMemoryTask findHypernymsTask;
    private FindTypesInMemoryTask findTypesTask;
    private FindCategoriesInMemoryTask findCategories;
    private Map<Integer, List<String>> wordsForTopics;
    private Map<Integer, List<Double>> wordCoveragesForTopics;
    private List<String> topicLabels;

    public void loadFile(String file) throws IOException, ClassNotFoundException {
        System.out.println("Loading topic model ("+file+")...");
        ldaModel.readFromFile(file);
        System.out.println("Loaded.");
        wordsForTopics = Cache.getWordsForTopics(ldaModel);
        wordCoveragesForTopics = Cache.getWordCoveragesForTopics(ldaModel);
        topicLabels = Cache.getTopicLabels(ldaModel);
    }

    public double[] predictTopicCoverage(String spotlightAnnotation) {
        InputToDataset reader = new InputToDataset(spotlightAnnotation);
        EmptyFinisher finisher = new EmptyFinisher();
        Pipeline pipeline = new Pipeline(reader, finisher);
        pipeline.addTask(new FindLemmasTask());
        pipeline.addTask(findHypernymsTask);
        pipeline.doWork();
        String featureVec = inputGenerator.generateFeatureVector(finisher.getProcessedInstance());
        double[] prediction = ldaModel.predict(featureVec);
        return prediction;
    }

    public List<String> getWordsForTopic(int topicId) {
        return wordsForTopics.get(topicId);
    }

    public List<Double> getWordCoveragesForTopic(int topicId) {
        return wordCoveragesForTopics.get(topicId);
    }

    public String getLabel(int topicId) {
        return topicLabels.get(topicId);
    }
}
