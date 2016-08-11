package org.dbpedia.topics;

import org.apache.commons.cli.ParseException;
import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.dataset.models.impl.DBpediaAbstract;
import org.dbpedia.topics.dataset.readers.Reader;
import org.dbpedia.topics.dataset.readers.StreamingReader;
import org.dbpedia.topics.dataset.readers.impl.DBpediaAbstractsReader;
import org.dbpedia.topics.dataset.readers.impl.WikipediaDumpStreamingReader;
import org.dbpedia.topics.io.MongoWrapper;
import org.dbpedia.topics.modelling.HierarchicalLdaModel;
import org.dbpedia.topics.modelling.LDAInputGenerator;
import org.dbpedia.topics.modelling.LdaModel;
import org.dbpedia.topics.pipeline.*;
import org.dbpedia.topics.pipeline.impl.*;
import org.dbpedia.topics.rdfencoder.RDFEncoder;
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
    public static void main(String[] args) throws URISyntaxException, IOException {
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
        PipelineFinisher finisher;
        IPipeline pipeline;

        String finisherStr = opts.getOptionValue(CmdLineOpts.FINISHER);
        if (finisherStr.equals("mongo")) {
            System.out.println("mongodb finisher");
            finisher = new MongoDBInsertFinisher(Config.MONGO_SERVER, Config.MONGO_PORT,
                    opts.hasOption(CmdLineOpts.DONT_STORE_TEXT));
        }else if (finisherStr.equals("dummy")) {
            System.out.println("dummy finisher");
            finisher = new TestFinisher();
        }
        else {
            throw new IllegalArgumentException("Unknown finisher: " + finisherStr);
        }

        String readerStr = opts.getOptionValue(CmdLineOpts.READER);
        if (readerStr.equals("abstracts")) {
            Reader reader = new DBpediaAbstractsReader(Config.ABSTRACTS_TRIPLE_FILE);
            pipeline = new Pipeline(reader, finisher);
        } else if (readerStr.equals("wikidump")) {
            StreamingReader reader = new WikipediaDumpStreamingReader(Config.WIKI_AS_XML_FOLDER);
            pipeline = new StreamingPipeline(reader, finisher);
        } else {
            throw new IllegalArgumentException("Unknown reader: " + readerStr);
        }
        System.out.println("Reader: " + readerStr);

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
            finisher.close();
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

        String[] features = opts.getOptionValues(CmdLineOpts.FEATURES);
        String[] strNumTopicsArr = opts.getOptionValues(CmdLineOpts.NUM_TOPICS);
        int[] numTopicsArr = new int[strNumTopicsArr.length];
        for (int i = 0; i < strNumTopicsArr.length; i++) {
            numTopicsArr[i] = Integer.valueOf(strNumTopicsArr[i]);
        }

        LDAInputGenerator inputGenerator = new LDAInputGenerator(features);
        MongoWrapper mongo = new MongoWrapper(Config.MONGO_SERVER, Config.MONGO_PORT);

        MorphiaIterator<DBpediaAbstract, DBpediaAbstract> iter = mongo.getAllRecordsIterator(DBpediaAbstract.class);
        List<String> featureMatrix = new ArrayList<>();
        for (DBpediaAbstract dbAbstract : iter) {
            featureMatrix.add(inputGenerator.generateFeatureVector(dbAbstract));
        }

        String outputDir = "models-lda-abstracts";
        new File(outputDir).mkdirs();

        for (int numTopics : numTopicsArr) {
            LdaModel ldaModel = new LdaModel(features);
            ldaModel.createModel(featureMatrix, numTopics, 1000, 16);

            String featureSetDescriptor = Stream.of(features).collect(Collectors.joining("-"));
            String outputModelFile = String.format("%s/%d-%s.ser", outputDir, numTopics, featureSetDescriptor);
            ldaModel.saveToFile(outputModelFile);
            String outputCsvFile = String.format("%s/%d-%s.csv", outputDir, numTopics, featureSetDescriptor);
            int numTopicDescrWords = Integer.valueOf(opts.getOptionValue(CmdLineOpts.NUM_TOPIC_WORDS, "20"));
            ldaModel.describeTopicModel(outputCsvFile, numTopicDescrWords);
        }
    }

    private static void runHLDA(CmdLineOpts opts) throws IOException {
        String[] features = opts.getOptionValues(CmdLineOpts.FEATURES);

        LDAInputGenerator inputGenerator = new LDAInputGenerator(features);
        MongoWrapper mongo = new MongoWrapper(Config.MONGO_SERVER, Config.MONGO_PORT);

        MorphiaIterator<DBpediaAbstract, DBpediaAbstract> iter = mongo.getAllRecordsIterator(DBpediaAbstract.class);
        List<String> featureMatrix = new ArrayList<>();
        for (DBpediaAbstract dbAbstract : iter) {
            featureMatrix.add(inputGenerator.generateFeatureVector(dbAbstract));
        }

        String outputDir = "models-hlda-abstracts";
        new File(outputDir).mkdirs();

        String[] strNumLevelsArr = opts.getOptionValues(CmdLineOpts.NUM_LEVELS);
        int[] numLevelsArr = new int[strNumLevelsArr.length];
        for (int i = 0; i < strNumLevelsArr.length; i++) {
            numLevelsArr[i] = Integer.valueOf(strNumLevelsArr[i]);
        }

        for (int numLevels : numLevelsArr) {
            HierarchicalLdaModel hldaModel = new HierarchicalLdaModel(features);
            hldaModel.setNumWords(Integer.valueOf(opts.getOptionValue(CmdLineOpts.NUM_TOPIC_WORDS, "20")));
            hldaModel.createModel(featureMatrix, 1000, numLevels);

            String featureSetDescriptor = Stream.of(features).collect(Collectors.joining("-"));
            String outputModelFile = String.format("%s/%s.ser", outputDir, featureSetDescriptor);
            hldaModel.saveToFile(outputModelFile);
            String outputCsvFile = String.format("%s/%s.csv", outputDir, featureSetDescriptor);
            hldaModel.describeTopicModel(outputCsvFile);
        }
    }

    private static void startEncoding(CmdLineOpts opts) throws IOException {
        String algorithm = opts.getOptionValue(CmdLineOpts.MODELLING_ALGORITHM);
        String modelDir = opts.getOptionValue(CmdLineOpts.INPUT_DIR);

        Stream<Path> stream = Files.walk(Paths.get(modelDir))
                .filter(path -> path.toFile().isFile() && path.toString().endsWith("ser"));

        String[] strNumTopicsArr = opts.getOptionValues(CmdLineOpts.NUM_TOPICS);
        String outputFile = opts.getOptionValue(CmdLineOpts.OUTPUT);
        String outputFormat = opts.getOptionValue(CmdLineOpts.OUTPUT_FORMAT, "NT");
        int numDescribingWords = Integer.valueOf(opts.getOptionValue(CmdLineOpts.NUM_TOPIC_WORDS, "10"));

        if (strNumTopicsArr != null) {
            stream = stream.filter(path -> {
                String filename = path.getFileName().toString();
                for (String s : strNumTopicsArr) {
                    if (filename.startsWith(s+"-")) {
                        return true;
                    }
                }

                return false;
            });
        }

        MongoWrapper mongo = new MongoWrapper(Config.MONGO_SERVER, Config.MONGO_PORT);

        stream.forEach(path -> {
            String filenameNoExt = path.getFileName().toString().replace(".ser", "");
            String[] features = filenameNoExt.split("-", 2)[1].split("-");
            LdaModel ldaModel = new LdaModel(features);
            try {
                ldaModel.readFromFile(path.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            RDFEncoder encoder = new RDFEncoder(ldaModel);
            encoder.encodeTopics(numDescribingWords);

            LDAInputGenerator inputGenerator = new LDAInputGenerator(features);
            MorphiaIterator<DBpediaAbstract, DBpediaAbstract> iter = mongo.getAllRecordsIterator(DBpediaAbstract.class);
            for (DBpediaAbstract dbAbstract : iter) {
                String input = inputGenerator.generateFeatureVector(dbAbstract);
                encoder.encodeOneObservation(dbAbstract.getUri(), input);
            }

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
        });
    }

    private static void startDump(CmdLineOpts opts) throws IOException {
        String outputDirStr = opts.getOptionValue(CmdLineOpts.OUTPUT);
        new File(outputDirStr).mkdirs();
        MongoWrapper mongo = new MongoWrapper(Config.MONGO_SERVER, Config.MONGO_PORT);
        String readerStr = opts.getOptionValue(CmdLineOpts.READER);
        System.out.println("Saving dump to " + outputDirStr);
        int partitionSize = Integer.parseInt(opts.getOptionValue(CmdLineOpts.CHUNK_SIZE, "250000"));
        List<Instance> dump = new ArrayList<>(partitionSize);
        int ct = 0;
        int part = 0;
        if (readerStr.equals("abstracts")) {
            MorphiaIterator<DBpediaAbstract, DBpediaAbstract> iter = mongo.getAllRecordsIterator(DBpediaAbstract.class);
            for (DBpediaAbstract dbAbstract : iter) {
                if (++ct % 25000 == 0) {
                    System.out.println(ct);
                }
                dump.add(dbAbstract);
                if (dump.size() == partitionSize) {
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(outputDirStr, (part++)+".ser")))) {
                        oos.writeObject(dump);
                    }
                    dump.clear();
                }
            }
        } else if (readerStr.equals("wikidump")) {
            throw new IllegalArgumentException("Not yet implemented: " + readerStr);
        } else {
            throw new IllegalArgumentException("Unknown reader: " + readerStr);
        }


        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(outputDirStr, (part++)+".ser")))) {
            oos.writeObject(dump);
        }
    }

    private static void startElasticImport(CmdLineOpts opts) throws IOException {
        Path inputDir = Paths.get(opts.getOptionValue(CmdLineOpts.INPUT_DIR)).toAbsolutePath();
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

    private static void multiThreadedPipeline(int numWorkers) throws URISyntaxException {
        long numLines = Utils.getNumberOfLines("/media/data/datasets/gsoc/long_abstracts_en.ttl");
        long batchSize = numLines/numWorkers;

        for (int i = 0; i < numWorkers; i++) {
            long start = batchSize*i;
            long end = i == numWorkers-1 ? numLines : batchSize*(i+1);

            Reader reader = new DBpediaAbstractsReader("/media/data/datasets/gsoc/long_abstracts_en.ttl", start, end);
            PipelineFinisher finisher = new MongoDBInsertFinisher(Config.MONGO_SERVER, Config.MONGO_PORT);

            Pipeline pipeline = new Pipeline(reader, finisher);
            pipeline.addTask(new FindLemmasTask());
            pipeline.addTask(new AnnotateTask(Config.SPOTLIGHT_ENDPOINT));
            pipeline.addTask(new FindTypesTask(Config.DBPEDIA_ENDPOINT));
            pipeline.addTask(new FindCategoriesTask(Config.DBPEDIA_ENDPOINT));
            pipeline.addTask(new FindHypernymsTask(Config.HYPERNYMS_ENDPOINT));


            PipelineThread task = new PipelineThread(reader, finisher, pipeline);
            Thread thread = new Thread(task);
            thread.start();
        }
    }
}
