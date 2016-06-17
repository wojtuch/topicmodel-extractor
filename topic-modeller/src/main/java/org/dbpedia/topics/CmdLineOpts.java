package org.dbpedia.topics;

import org.apache.commons.cli.*;

/**
 * Created by wlu on 26.05.16.
 */
public class CmdLineOpts {

    public static final Option PREPROCESSING_PIPELINE = Option.builder("p").longOpt("start-pipeline")
            .desc("Starts the pipeline to prepare documents for topic modelling. Requires specifying the reader, the tasks and the finisher.").build();

    public static final Option TASKS = Option.builder("t").longOpt("tasks")
            .desc("Which tasks should the pipeline perform. Possible values are 'lemma', 'annotate' [using Spotlight], 'types', 'categories', 'hypernyms'.")
            .numberOfArgs(5).hasArgs().argName("tasks...").build();

    public static final Option READER = Option.builder("r").longOpt("reader")
            .desc("Which data should the pipeline be run on? Possible values are 'abstracts' and 'wikidump'.").hasArgs().argName("reader").build();

    public static final Option FINISHER = Option.builder("f").longOpt("finisher")
            .desc("How should the pipeline finish. Possible values are 'mongo'.").hasArgs().argName("reader").build();

    public static final Option IN_MEMORY = Option.builder("m").longOpt("in-memory")
            .desc("read the triple files specified in props.properties into memory to provide much faster annotation. be sure to provide sufficient heap size with -Xmx64g")
            .build();

    public static final Option HELP = Option.builder("h").longOpt("help").desc("Shows this message.").build();

    private Options options = new Options();
    private CommandLine cmd;
    private String cmdName = "topic-modeller";

    public CmdLineOpts() {
        Option[] optsArr = new Option[]{PREPROCESSING_PIPELINE, TASKS, READER, FINISHER, IN_MEMORY, HELP};
        for (Option option : optsArr) {
            this.options.addOption(option);
        }
    }

    public void parse(String[] args) throws ParseException {
        cmd = new DefaultParser().parse(options, args);
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(cmdName, options, true);
    }

    public boolean hasOption(Option opt) {
        return cmd.hasOption(opt.getOpt());
    }


    public String getOptionValue(Option opt) {
        return cmd.getOptionValue(opt.getOpt());
    }

    public String[] getOptionValues(Option opt) {
        return cmd.getOptionValues(opt.getOpt());
    }

    public boolean isHelp() {
        return hasOption(HELP);
    }
}
