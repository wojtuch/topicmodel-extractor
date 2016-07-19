package org.dbpedia.topics.inference;

import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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

    public void loadFile(String file) throws IOException, ClassNotFoundException {
        ldaModel.readFromFile(file);
    }

    public double[] predictTopicCoverage(String spotlightAnnotation) {
        InputToDataset reader = new InputToDataset(spotlightAnnotation);
        EmptyFinisher finisher = new EmptyFinisher();
        Pipeline pipeline = new Pipeline(reader, finisher);
        pipeline.addTask(new FindLemmasTask());
        pipeline.addTask(findHypernymsTask);
        pipeline.doWork();
        double[] prediction = ldaModel.predict(inputGenerator.generateFeatureVector(finisher.getProcessedInstance()));
        for (int i = 0; i < prediction.length; i++) {
            prediction[i] = new BigDecimal(prediction[i]).setScale(4, RoundingMode.HALF_UP).doubleValue();
        }
        return prediction;
    }

    public List<String> getWordsForTopic(int topicId, int topN) {
        List<String> result = new ArrayList<>();

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = ldaModel.getModel().getAlphabet();

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = ldaModel.getModel().getSortedWords();

        Iterator<IDSorter> iterator = topicSortedWords.get(topicId).iterator();

        int rank = 0;
        while (iterator.hasNext() && rank < topN) {
            IDSorter idCountPair = iterator.next();
            result.add((String) dataAlphabet.lookupObject(idCountPair.getID()));
            rank++;
        }

        return result;
    }

    public List<Double> getWordCoveragesForTopic(int topicId, int topN) {
        List<Double> result = new ArrayList<>();

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = ldaModel.getModel().getSortedWords();

        Iterator<IDSorter> iterator = topicSortedWords.get(topicId).iterator();

        double sumWeights = ldaModel.getModel().getSortedWords().get(topicId).stream().mapToDouble(ids -> ids.getWeight()).sum();

        int rank = 0;
        while (iterator.hasNext() && rank < topN) {
            IDSorter idCountPair = iterator.next();
            result.add(new BigDecimal(idCountPair.getWeight()*100/sumWeights).setScale(4, RoundingMode.HALF_UP).doubleValue());
            rank++;
        }

        return result;
    }
}
