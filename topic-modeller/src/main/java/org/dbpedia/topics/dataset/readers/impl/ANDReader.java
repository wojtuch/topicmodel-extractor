package org.dbpedia.topics.dataset.readers.impl;

import org.apache.commons.io.FileUtils;
import org.dbpedia.topics.dataset.models.Dataset;
import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.dataset.models.impl.*;
import org.dbpedia.topics.dataset.readers.Reader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by wlu on 29.05.16.
 */
public class ANDReader extends Reader {

    private String folderWithCategoryCSVs;
    private boolean and2382;

    /**
     * Constructs a Reader that reads specified number of abstracts from a directory containing txt files with BBC articles.
     * @param folderWithCategoryCSVs
     */
    public ANDReader(String folderWithCategoryCSVs, boolean and2382) {
        this.folderWithCategoryCSVs = folderWithCategoryCSVs;
        this.and2382 = and2382;
    }

    /*
        Tested two methods for reading the dataset:
            - using Apache Jena
            - using regular expressions
        Regex method is faster, approx. 108149ms vs. 158681ms.
     */
    @Override
    public Dataset readDataset() {
        Dataset dataset = new ANDArticleDataset();
        for (File csvFile : new File(folderWithCategoryCSVs).listFiles((f,s) -> s.endsWith(".csv"))) {
            String category = csvFile.getAbsoluteFile().getName().replace(".csv", "");
            Stream<String> linesStr = Stream.of();
            try {
                linesStr = Files.lines(Paths.get(csvFile.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            linesStr.forEach(line -> {
                String[] split = line.split(";", 2);
                if (split.length == 2) {
                    String uri = split[0];
                    String text = split[1];
                    Instance article = and2382 ? new AND2382Article() : new ANDArticle();
                    article.setText(text);
                    article.setTitle(uri);
                    article.setLabel(category);
                    article.setUuid(UUID.randomUUID().toString());
                    dataset.addDocument(article);
                }
            });
        }

        return dataset;
    }
}
