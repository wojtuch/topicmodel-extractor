package org.dbpedia.topics.dataset.readers.impl;

import org.apache.commons.io.FileUtils;
import org.dbpedia.topics.dataset.models.Dataset;
import org.dbpedia.topics.dataset.models.impl.BBCArticle;
import org.dbpedia.topics.dataset.models.impl.BBCArticleDataset;
import org.dbpedia.topics.dataset.models.impl.DBpediaAbstract;
import org.dbpedia.topics.dataset.models.impl.DBpediaAbstractsDataset;
import org.dbpedia.topics.dataset.readers.Reader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by wlu on 29.05.16.
 */
public class BBCReader extends Reader {

    private String inputDir;

    /**
     * Constructs a Reader that reads specified number of abstracts from a directory containing txt files with BBC articles.
     * @param inputDir
     */
    public BBCReader(String inputDir) {
        this.inputDir = inputDir;
    }

    /*
        Tested two methods for reading the dataset:
            - using Apache Jena
            - using regular expressions
        Regex method is faster, approx. 108149ms vs. 158681ms.
     */
    @Override
    public Dataset readDataset() {
        Dataset dataset = new BBCArticleDataset();
        Collection<File> articleFiles = FileUtils.listFiles(new File(this.inputDir), new String[]{"txt"}, true);

        for (File articleFile : articleFiles) {
            Stream<String> linesStream = Stream.empty();
            try {
                linesStream = Files.lines(Paths.get(articleFile.getAbsolutePath()), Charset.forName("ISO-8859-1"));
            } catch (IOException e) {
                System.err.println("Can not read file: " + articleFile.getAbsolutePath());
                e.printStackTrace();
            }

            List<String> lines = linesStream.filter(line -> line.trim().length() > 0).collect(Collectors.toList());
            String title = lines.get(0).trim();

            String text = lines.subList(1, lines.size()).stream().collect(Collectors.joining(" "));

            String label = articleFile.getAbsolutePath().replace(this.inputDir, "");
            label = label.substring(0, label.indexOf("/"));

            BBCArticle article = new BBCArticle();
            article.setTitle(title);
            article.setText(text);
            article.setLabel(label);
            article.setUuid(UUID.randomUUID().toString());

            dataset.addDocument(article);
        }

        return dataset;
    }
}
