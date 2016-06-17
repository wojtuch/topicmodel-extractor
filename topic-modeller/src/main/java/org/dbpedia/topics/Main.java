package org.dbpedia.topics;

import org.apache.commons.cli.ParseException;
import org.dbpedia.topics.dataset.readers.Reader;
import org.dbpedia.topics.dataset.readers.impl.DBpediaAbstractsReader;
import org.dbpedia.topics.pipeline.Pipeline;
import org.dbpedia.topics.pipeline.PipelineFinisher;
import org.dbpedia.topics.pipeline.PipelineThread;
import org.dbpedia.topics.pipeline.impl.*;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wlu on 26.05.16.
 */
public class Main {

    public static void main(String[] args) throws URISyntaxException {
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

    }

    private static void startPipeline(CmdLineOpts opts) throws URISyntaxException {
        Reader reader;
        PipelineFinisher finisher;

        String finisherStr = opts.getOptionValue(CmdLineOpts.FINISHER);
        if (finisherStr.equals("mongo")) {
            System.out.println("mongo");
            finisher = new MongoDBInsertFinisher(Config.MONGO_SERVER, Config.MONGO_PORT);
        }
        else {
            throw new IllegalArgumentException("Unknown finisher: " + finisherStr);
        }

        String readerStr = opts.getOptionValue(CmdLineOpts.READER);
        if (readerStr.equals("abstracts")) {
            System.out.println("abstracts");
            reader = new DBpediaAbstractsReader(Config.ABSTRACTS_TRIPLE_FILE);
        }
        else {
            throw new IllegalArgumentException("Unknown finisher: " + finisherStr);
        }

        Pipeline pipeline = new Pipeline(reader, finisher);

        List<String> tasks = Arrays.asList(opts.getOptionValues(CmdLineOpts.TASKS));
        System.out.println(tasks);

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

//        try {
//            pipeline.doWork();
//        }
//        finally {
//            finisher.close();
//            reader.close();
//        }
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
