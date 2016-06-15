package org.dbpedia.topics.modelling;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.dbpedia.topics.Constants;
import org.dbpedia.topics.io.StopWords;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by wlu on 26.05.16.
 */
public class LdaModel implements TopicModel {

    /**
     * defines if
     * words (words)
     * entities (entities)
     * rdf-types (types)
     * dcTerm-subjects (categories)
     * hypernyms (hypernyms)
     * should be used as input for LDA.
     * See Constants.FEATURE_DESCRIPTOR_* for allowed values.
     */
    private List<String> features;
    private ParallelTopicModel model;
    private ArrayList<Pipe> pipeList = new ArrayList<>();

    private String[] blacklistEntities = new String[]{
    };

    private String[] blacklistTypes = new String[]{
    };

    private String[] blacklistCategories = new String[]{
    };

    private String[] blacklistHypernyms = new String[]{
    };

    public LdaModel(String... topicModes) {
        this.features = Arrays.asList(topicModes).stream().filter(t -> t!=null).collect(Collectors.toList());

        // Pipes: lowercase, remove stopwords, map to features

        TokenSequenceRemoveStopwords tsrs = new TokenSequenceRemoveStopwords();
        tsrs.setCaseSensitive(true);

        if (this.features.contains(Constants.FEATURE_DESCRIPTOR_WORDS)) {
            tsrs.addStopWords(StopWords.STOPWORDS);
        }
        if (this.features.contains(Constants.FEATURE_DESCRIPTOR_ENTITIES)) {
            tsrs.addStopWords(blacklistEntities);
        }
        if (this.features.contains(Constants.FEATURE_DESCRIPTOR_TYPES)) {
            tsrs.addStopWords(blacklistTypes);
        }
        if (this.features.contains(Constants.FEATURE_DESCRIPTOR_CATEGORIES)) {
            tsrs.addStopWords(blacklistCategories);
        }
        if (this.features.contains(Constants.FEATURE_DESCRIPTOR_HYPERNYMS)) {
            tsrs.addStopWords(blacklistHypernyms);
        }

        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\d\\p{L}\\p{P}]+[\\p{L}\\d\\p{P}]")));

        pipeList.add(tsrs);

        pipeList.add(new TokenSequence2FeatureSequence());
    }

    public void createModel(List<String> input, int numTopics, int numIterations, int numThreads) throws IOException {
        InstanceList instances = new InstanceList (new SerialPipes(pipeList));
        instances.addThruPipe(new ArrayIterator(input));

        model = new ParallelTopicModel(numTopics, 1.0, 0.01);
        model.addInstances(instances);
        model.setNumThreads(numThreads);
        model.setNumIterations(numIterations);
        model.estimate();
    }

    public void saveToFile(String outputfile) throws IOException{
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputfile))){
            oos.writeObject(model);
        }
    }

    public void readFromFile(String inputfile) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputfile))){
            this.model = (ParallelTopicModel) ois.readObject();
        }
    }

    @Override
    public double[] predict(String text) {
        InstanceList instances = new InstanceList(new SerialPipes(pipeList));
        instances.addThruPipe(new Instance(text, null, "test instance", null));

        TopicInferencer inferencer = model.getInferencer();
        double[] probabilities = inferencer.getSampledDistribution(instances.get(0), 10, 1, 5);
        return probabilities;
    }
}
