package org.dbpedia.topics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.cli.ParseException;
import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.dataset.models.impl.*;
import org.dbpedia.topics.dataset.readers.Reader;
import org.dbpedia.topics.dataset.readers.StreamingReader;
import org.dbpedia.topics.dataset.readers.impl.ANDReader;
import org.dbpedia.topics.dataset.readers.impl.BBCReader;
import org.dbpedia.topics.dataset.readers.impl.DBpediaAbstractsReader;
import org.dbpedia.topics.dataset.readers.impl.WikipediaDumpStreamingReader;
import org.dbpedia.topics.io.MongoWrapper;
import org.dbpedia.topics.modelling.HLDAWrapper;
import org.dbpedia.topics.modelling.MalletInputGenerator;
import org.dbpedia.topics.modelling.LDAWrapper;
import org.dbpedia.topics.modelling.ITopicModelWrapper;
import org.dbpedia.topics.pipeline.IPipeline;
import org.dbpedia.topics.pipeline.Pipeline;
import org.dbpedia.topics.pipeline.StreamingPipeline;
import org.dbpedia.topics.pipeline.impl.*;
import org.dbpedia.topics.rdfencoder.HLDA2RDFEncoder;
import org.dbpedia.topics.rdfencoder.IEncoder;
import org.dbpedia.topics.rdfencoder.LDA2RDFEncoder;
import org.dbpedia.topics.utils.Utils;
import org.mongodb.morphia.query.MorphiaIterator;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by wlu on 26.05.16.
 */
public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, ClassNotFoundException {
        CmdLineOpts opts = new CmdLineOpts();

        try {
            opts.parse(args);
        } catch (ParseException e) {
            e.printStackTrace();
            opts.printHelp();
            return;
        }

        if (opts.isHelp()) {
            opts.printHelp();
            return;
        }

        if (opts.hasOption(CmdLineOpts.PREPROCESSING_PIPELINE)) {
            if (opts.hasOption(CmdLineOpts.READER) &&
                    opts.hasOption(CmdLineOpts.TASKS) &&
                    opts.hasOption(CmdLineOpts.FINISHER)) {
                System.out.println("Starting pipeline");
                startPipeline(opts);
            }
            else {
                opts.printHelp();
            }
        }
        else if  (opts.hasOption(CmdLineOpts.TOPIC_MODELLING)) {
            System.out.println("Starting topic modelling");
            startTopicModelling(opts);
        }
        else if  (opts.hasOption(CmdLineOpts.ENCODE_MINED_TOPICS)) {
            System.out.println("Starting the encoder");
            startEncoding(opts);
        }
        else if (opts.hasOption(CmdLineOpts.DUMP_MONGO)) {
            System.out.println("Dump mongo to disc");
            startDump(opts);
        }
        else if (opts.hasOption(CmdLineOpts.IMPORT_ELASTIC)) {
            System.out.println("Import to elastic");
            startElasticImport(opts);
        }
        else {
            opts.printHelp();
        }
    }

    private static void startPipeline(CmdLineOpts opts) throws URISyntaxException {
        IPipeline pipeline;

        String readerStr = opts.getOptionValue(CmdLineOpts.READER);
        if (readerStr.equals("abstracts")) {
            Reader reader = new DBpediaAbstractsReader(Config.ABSTRACTS_TRIPLE_FILE);
            pipeline = new Pipeline(reader);
        } else if (readerStr.equals("wikidump")) {
            StreamingReader reader = new WikipediaDumpStreamingReader(Config.WIKI_AS_XML_FOLDER);
            pipeline = new StreamingPipeline(reader);
        } else if (readerStr.equals("bbc")) {
            Reader reader = new BBCReader(Config.BBC_DIRECTORY);
            pipeline = new Pipeline(reader);
        } else if (readerStr.equals("and")) {
            Reader reader = new ANDReader(Config.AND_DIRECTORY, false);
            pipeline = new Pipeline(reader);
        } else if (readerStr.equals("and2382")) {
            Reader reader = new ANDReader(Config.AND2382_DIRECTORY, true);
            pipeline = new Pipeline(reader);
        } else {
            throw new IllegalArgumentException("Unknown reader: " + readerStr);
        }
        System.out.println("Reader: " + readerStr);


        String[] finishersStr = opts.getOptionValues(CmdLineOpts.FINISHER);
        for (String finisherStr : finishersStr) {
            if (finisherStr.equals("mongo")) {
                System.out.println("mongodb finisher");
                pipeline.addFinisher(new MongoDBInsertFinisher(Config.MONGO_SERVER, Config.MONGO_PORT,
                        opts.hasOption(CmdLineOpts.DONT_STORE_TEXT)));
            } else if (finisherStr.equals("json")) {
                System.out.println("json finisher");
                pipeline.addFinisher(new JsonDiskFinisher(opts.getOptionValue(CmdLineOpts.OUTPUT)));
            } else if (finisherStr.equals("dummy")) {
                System.out.println("dummy finisher");
                pipeline.addFinisher(new TestFinisher());
            } else {
                throw new IllegalArgumentException("Unknown finisher: " + finishersStr);
            }
        }

        List<String> tasks = Arrays.asList(opts.getOptionValues(CmdLineOpts.TASKS));
        System.out.println("Passed tasks: " + tasks);

        if (tasks.contains("lemma")) {
            pipeline.addTask(new FindLemmasTask());
        }
        if (tasks.contains("annotate")) {
            pipeline.addTask(new AnnotateTask(Config.SPOTLIGHT_ENDPOINT));
        }
        if (opts.hasOption(CmdLineOpts.IN_MEMORY)) {
            if (tasks.contains("types")) {
                pipeline.addTask(new FindTypesInMemoryTask(Config.TYPES_TRIPLE_FILE));
            }
            if (tasks.contains("categories")) {
                pipeline.addTask(new FindCategoriesInMemoryTask(Config.CATEGORIES_TRIPLE_FILE));
            }
            if (tasks.contains("hypernyms")) {
                pipeline.addTask(new FindHypernymsInMemoryTask(Config.HYPERNYMS_TRIPLE_FILE));
            }
        }
        else {
            if (tasks.contains("types")) {
                pipeline.addTask(new FindTypesTask(Config.DBPEDIA_ENDPOINT));
            }
            if (tasks.contains("categories")) {
                pipeline.addTask(new FindCategoriesTask(Config.DBPEDIA_ENDPOINT));
            }
            if (tasks.contains("hypernyms")) {
                pipeline.addTask(new FindHypernymsTask(Config.HYPERNYMS_ENDPOINT));
            }
        }

        try {
            pipeline.doWork();
        }
        finally {
            pipeline.close();
        }
    }

    private static void startTopicModelling(CmdLineOpts opts) throws IOException {
        String algorithm = opts.getOptionValue(CmdLineOpts.MODELLING_ALGORITHM);

        if (algorithm == null) {
            System.err.println("You must specify the algorithm you want to use for topic modelling!");
            opts.printHelp();
        } else if (algorithm.equals("lda")) {
            runLDA(opts);
        } else if (algorithm.equals("hlda")) {
            runHLDA(opts);
        } else {
            throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
    }

    private static void runLDA(CmdLineOpts opts) throws IOException {
        List<String[]> featuresCombinations = new ArrayList<>();

        if (opts.hasOption(CmdLineOpts.FEATURES)) {
            featuresCombinations.add(opts.getOptionValues(CmdLineOpts.FEATURES));
        }
        else {
            featuresCombinations.addAll(Utils.createPowerSet("w", "e", "t", "c", "h"));
        }

        String[] strNumTopicsArr = opts.getOptionValues(CmdLineOpts.NUM_TOPICS);
        int[] numTopicsArr = new int[strNumTopicsArr.length];
        for (int i = 0; i < strNumTopicsArr.length; i++) {
            numTopicsArr[i] = Integer.valueOf(strNumTopicsArr[i]);
        }
        System.out.println("Number(s) of topics: " + Arrays.toString(numTopicsArr));

        MongoWrapper mongo = new MongoWrapper(Config.MONGO_SERVER, Config.MONGO_PORT);

        List<Instance> input = new ArrayList<>();
        MorphiaIterator<Instance, Instance> iter;
        String readerStr = opts.getOptionValue(CmdLineOpts.READER);
        System.out.println("Reader: " + readerStr);
        switch (readerStr) {
            case "abstracts":
                iter = mongo.getAllRecordsIterator(DBpediaAbstract.class);
                break;
            case "wikidump":
                iter = mongo.getAllRecordsIterator(WikipediaArticle.class);
                break;
            case "bbc":
                iter = mongo.getAllRecordsIterator(BBCArticle.class);
                break;
            case "and":
                iter = mongo.getAllRecordsIterator(ANDArticle.class);
                break;
            case "and2382":
                iter = mongo.getAllRecordsIterator(AND2382Article.class);
                break;
            default:
                throw new IllegalArgumentException("Unknown reader: " + readerStr);
        }
        for (Instance instance : iter) {
            input.add(instance);
        }

        for (String[] features : featuresCombinations) {
            System.out.println("Used features: "+Arrays.toString(features));
            MalletInputGenerator inputGenerator = new MalletInputGenerator(features);

            List<String> featureMatrix = new ArrayList<>();
            for (Instance instance : input) {
                featureMatrix.add(inputGenerator.generateFeatureVector(instance));
            }

            String outputDir = opts.getOptionValue(CmdLineOpts.OUTPUT);
            new File(outputDir).mkdirs();

            for (int numTopics : numTopicsArr) {
                LDAWrapper ldaModel = new LDAWrapper(features);
                ldaModel.createModel(featureMatrix, numTopics, Config.LDA_NUM_ITERATIONS, Config.LDA_NUM_THREADS);

                String featureSetDescriptor = Stream.of(features).collect(Collectors.joining("-"));
                String outputModelFile = String.format("%s/%d-%s.ser", outputDir, numTopics, featureSetDescriptor);
                ldaModel.saveToFile(outputModelFile);
//            String outputCsvFile = String.format("%s/%d-%s.csv", outputDir, numTopics, featureSetDescriptor);
//            int numTopicDescrWords = Integer.valueOf(opts.getOptionValue(CmdLineOpts.NUM_TOPIC_WORDS, "20"));
//            ldaModel.describeTopicModel(outputCsvFile, numTopicDescrWords);
            }
        }
    }

    private static void runHLDA(CmdLineOpts opts) throws IOException {
        List<String[]> featuresCombinations = new ArrayList<>();
        if (opts.hasOption(CmdLineOpts.FEATURES)) {
            featuresCombinations.add(opts.getOptionValues(CmdLineOpts.FEATURES));
        } else {
            featuresCombinations.addAll(Utils.createPowerSet("w", "e", "t", "c", "h"));
        }

        MongoWrapper mongo = new MongoWrapper(Config.MONGO_SERVER, Config.MONGO_PORT);

        List<Instance> input = new ArrayList<>();
        MorphiaIterator<Instance, Instance> iter;
        String readerStr = opts.getOptionValue(CmdLineOpts.READER);
        System.out.println("Reader: " + readerStr);
        switch (readerStr) {
            case "abstracts":
                iter = mongo.getAllRecordsIterator(DBpediaAbstract.class);
                break;
            case "wikidump":
                iter = mongo.getAllRecordsIterator(WikipediaArticle.class);
                break;
            case "bbc":
                iter = mongo.getAllRecordsIterator(BBCArticle.class);
                break;
            case "and":
                iter = mongo.getAllRecordsIterator(ANDArticle.class);
                break;
            case "and2382":
                iter = mongo.getAllRecordsIterator(AND2382Article.class);
                break;
            default:
                throw new IllegalArgumentException("Unknown reader: " + readerStr);
        }
        for (Instance instance : iter) {
            input.add(instance);
        }

        String outputDir = opts.getOptionValue(CmdLineOpts.OUTPUT);
        new File(outputDir).mkdirs();

        for (String[] features : featuresCombinations) {
            MalletInputGenerator inputGenerator = new MalletInputGenerator(features);

            List<String> featureMatrix = new ArrayList<>();
            for (Instance instance : input) {
                featureMatrix.add(inputGenerator.generateFeatureVector(instance));
            }

            String[] strNumLevelsArr = opts.getOptionValues(CmdLineOpts.NUM_LEVELS);
            int[] numLevelsArr = new int[strNumLevelsArr.length];
            for (int i = 0; i < strNumLevelsArr.length; i++) {
                numLevelsArr[i] = Integer.valueOf(strNumLevelsArr[i]);
            }

            for (int numLevels : numLevelsArr) {
                HLDAWrapper hldaModel = new HLDAWrapper(features);
                hldaModel.setNumWords(Integer.valueOf(opts.getOptionValue(CmdLineOpts.NUM_TOPIC_WORDS, "20")));
                hldaModel.createModel(featureMatrix, Config.LDA_NUM_ITERATIONS, numLevels);

                String featureSetDescriptor = Stream.of(features).collect(Collectors.joining("-"));
                String outputModelFile = String.format("%s/%s-%s.ser", outputDir, numLevels, featureSetDescriptor);
                hldaModel.saveToFile(outputModelFile);
                String outputCsvFile = String.format("%s/%d-%s.csv", outputDir, numLevels, featureSetDescriptor);
                hldaModel.describeTopicModel(outputCsvFile);
            }
        }
    }

    private static void startEncoding(CmdLineOpts opts) throws IOException, ClassNotFoundException {
        Path modelPath = Paths.get(opts.getOptionValue(CmdLineOpts.INPUT));

        String outputFormat = opts.getOptionValue(CmdLineOpts.OUTPUT_FORMAT, "NT");
        int numDescribingWords = Integer.valueOf(opts.getOptionValue(CmdLineOpts.NUM_TOPIC_WORDS, "10"));

        String filenameNoExt = modelPath.getFileName().toString().replace(".ser", "");
        String[] features = filenameNoExt.split("-", 2)[1].split("-");
        ITopicModelWrapper topicModel;
        IEncoder encoder;

        String algorithm = opts.getOptionValue(CmdLineOpts.MODELLING_ALGORITHM);
        if (algorithm == null) {
            System.err.println("You must specify the algorithm you want to use for topic modelling!");
            opts.printHelp();
            return;
        } else if (algorithm.equals("lda")) {
            topicModel = new LDAWrapper(features);
            encoder = new LDA2RDFEncoder((LDAWrapper)topicModel);
        } else if (algorithm.equals("hlda")) {
            topicModel = new HLDAWrapper(features);
            encoder = new HLDA2RDFEncoder((HLDAWrapper)topicModel);
        } else {
            throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
        topicModel.readFromFile(modelPath.toString());
        encoder.encodeTopics(numDescribingWords);

        if (!algorithm.equals("hlda")) {
            MongoWrapper mongo = new MongoWrapper(Config.MONGO_SERVER, Config.MONGO_PORT);
            MorphiaIterator<Instance, Instance> iter;
            String readerStr = opts.getOptionValue(CmdLineOpts.READER);
            System.out.println("Reader: " + readerStr);
            switch (readerStr) {
                case "abstracts":
                    iter = mongo.getAllRecordsIterator(DBpediaAbstract.class);
                    break;
                case "wikidump":
                    iter = mongo.getAllRecordsIterator(WikipediaArticle.class);
                    break;
                case "bbc":
                    iter = mongo.getAllRecordsIterator(BBCArticle.class);
                    break;
                case "and":
                    iter = mongo.getAllRecordsIterator(ANDArticle.class);
                    break;
                case "and2382":
                    iter = mongo.getAllRecordsIterator(AND2382Article.class);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown reader: " + readerStr);
            }

            MalletInputGenerator inputGenerator = new MalletInputGenerator(features);

            for (Instance instance : iter) {
                String input = inputGenerator.generateFeatureVector(instance);
                encoder.encodeOneObservation(instance.getUuid(), input);
            }
        }

        String outputFile = opts.getOptionValue(CmdLineOpts.OUTPUT);
        if (outputFile == null) {
            System.out.println(encoder.toString(outputFormat));
        }
        else {
            try {
                Files.write(Paths.get(outputFile), encoder.toString(outputFormat).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void startDump(CmdLineOpts opts) throws IOException {
        String outputDirStr = opts.getOptionValue(CmdLineOpts.OUTPUT);
        new File(outputDirStr).mkdirs();
        MongoWrapper mongo = new MongoWrapper(Config.MONGO_SERVER, Config.MONGO_PORT);
        String readerStr = opts.getOptionValue(CmdLineOpts.READER);
        String outputFormatStr = opts.getOptionValue(CmdLineOpts.OUTPUT_FORMAT, "");
        int partitionSize = Integer.parseInt(opts.getOptionValue(CmdLineOpts.CHUNK_SIZE, "200000"));
        System.out.println(String.format("Saving dump to directory '%s' in chunks of size %d.", outputDirStr, partitionSize));
        List<Instance> dump = new ArrayList<>(partitionSize);
        int ct = 0;
        int part = 0;
        if (readerStr.equals("abstracts")) {
            MorphiaIterator<DBpediaAbstract, DBpediaAbstract> iter = mongo.getAllRecordsIterator(DBpediaAbstract.class);
            for (DBpediaAbstract dbAbstract : iter) {
                if (++ct % 25000 == 0) {
                    System.out.println(ct);
                }
                dbAbstract.setId(null);
                dump.add(dbAbstract);
                if (dump.size() == partitionSize) {
                    System.out.println("saving chunk");
                    if (outputFormatStr.toLowerCase().equals("json")) {
                        try (Writer writer = new FileWriter(new File(outputDirStr, (part++)+".json"))) {
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            gson.toJson(dump, writer);
                        }
                    }
                    else {
                        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(outputDirStr, (part++)+".ser")))) {
                            oos.writeObject(dump);
                        }
                    }
                    dump.clear();
                }
            }
        } else if (readerStr.equals("wikidump")) {
            throw new IllegalArgumentException("Not yet implemented: " + readerStr);
        } else {
            throw new IllegalArgumentException("Unknown reader: " + readerStr);
        }

        //write last part
        System.out.println("saving last chunk");
        if (outputFormatStr.toLowerCase().equals("json")) {
            try (Writer writer = new FileWriter(new File(outputDirStr, (part++)+".json"))) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(dump, writer);
            }
        }
        else {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(outputDirStr, (part++)+".ser")))) {
                oos.writeObject(dump);
            }
        }
    }

    private static void startElasticImport(CmdLineOpts opts) throws IOException {
        Path inputDir = Paths.get(opts.getOptionValue(CmdLineOpts.INPUT)).toAbsolutePath();
        System.out.println("Reading dump from " + inputDir.toString());

        ElasticSearchWorker elasticSearchWorker = new ElasticSearchWorker(Config.ELASTIC_SERVER, Config.ELASTIC_PORT);

        try {
            Files.walk(inputDir).filter(Files::isRegularFile).forEach(path -> {
                System.out.println("Reading " + path.toAbsolutePath().toString());
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
                    List<Instance> docs = (List<Instance>) ois.readObject();
                    System.out.println(String.format("Inserting %d documents", docs.size()));
                    elasticSearchWorker.insertInstances(docs, "abstracts");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
        finally {
            elasticSearchWorker.close();
        }
    }
}
