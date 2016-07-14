package org.dbpedia.topics.dataset.readers.impl;

import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.dataset.models.impl.WikipediaArticle;
import org.dbpedia.topics.dataset.readers.StreamingReader;
import org.dbpedia.topics.utils.ArticleCleaner;
import org.idio.wikipedia.dumps.EnglishWikipediaPage;
import org.idio.wikipedia.dumps.WikipediaPage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Created by wlu on 29.05.16.
 */
public class WikipediaDumpStreamingReader extends StreamingReader {

    private String pathToReadableWiki;

    public WikipediaDumpStreamingReader(String pathToReadableWiki) {
        this.pathToReadableWiki = pathToReadableWiki;
    }

    @Override
    public Stream<Instance> readDataset() {
        Stream<Instance> result = Stream.of();

        try {
            Stream<String> lines = Files.lines(Paths.get(pathToReadableWiki))
                    .skip(1)
                    .limit(1);
            Stream<String[]> titleText = lines.map(line -> {
                String[] splitByTab = line.split("\t",2);
                return splitByTab;
            });

            titleText = replaceLinksForIds(titleText);
            titleText = cleanArticles(titleText);
            result = titleText
                    .filter(tt -> !tt[1].equals("null"))
                    .map(tt -> {
                        Instance article = new WikipediaArticle();
                        article.setText(tt[1]);
                        article.setUri(tt[0]);
                        return article;
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private Stream<String[]> replaceLinksForIds(Stream<String[]> titleTextStream) {
        titleTextStream = titleTextStream.map(tt -> {
            String[] newTT = new String[2];
            newTT[0] = tt[0];
            newTT[1] = ArticleCleaner.replaceLinks(tt[1]);
            return newTT;
        });

        return titleTextStream;
    }

    private Stream<String[]> cleanArticles(Stream<String[]> titleTextStream) {
        titleTextStream = titleTextStream.map(tt -> {
            EnglishWikipediaPage wikiModel = new EnglishWikipediaPage();
            String pageContent = WikipediaPage.readPage(wikiModel, tt[1]);
            pageContent = ArticleCleaner.cleanStyle(pageContent);
            pageContent = ArticleCleaner.cleanCurlyBraces(pageContent);
            pageContent = pageContent.replaceAll("=", "").replaceAll("\\*", "");
            return new String[]{tt[0], pageContent.trim()};
        });

        return titleTextStream;
    }
}
