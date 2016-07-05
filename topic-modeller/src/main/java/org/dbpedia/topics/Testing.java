package org.dbpedia.topics;

import org.dbpedia.topics.dataset.models.impl.DBpediaAbstract;
import org.dbpedia.topics.io.MongoWrapper;
import org.dbpedia.topics.pipeline.impl.FindHypernymsInMemoryTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wlu on 15.06.16.
 */
public class Testing {
    public static void main(String[] args) {
        List<Integer> temp = new ArrayList<>();
        Utils.readSubjectObjectMappings("(<(.*?)>\\s*<http://purl\\.org/linguistics/gold/hypernym>\\s*<(.*?)>.*\\.)" +
                        "|" +
                        "(<(.*?)>\\s*<http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#type>\\s*<(.*?)>.*\\.)",
                Config.HYPERNYMS_TRIPLE_FILE)
            .forEach((str, lis) -> {
                temp.add(lis.size());
            });
        System.out.println(temp.stream().mapToInt(Integer::intValue).sum());
    }

    private static void testMongo(){
        MongoWrapper mongo = new MongoWrapper(Config.MONGO_SERVER, Config.MONGO_PORT);
        Map<String, List<String>> hypernyms = Utils.readSubjectObjectMappings("<(.*?)>\\s*<http://purl.org/linguistics/gold/hypernym>\\s*<(.*?)>.*\\.", Config.HYPERNYMS_TRIPLE_FILE);

        long start = System.currentTimeMillis();
        int ct = 0;
        for (DBpediaAbstract dBpediaAbstract : mongo.getAllRecordsIterator(DBpediaAbstract.class)) {
            System.out.println(dBpediaAbstract.getUri());
            System.out.println(dBpediaAbstract.getHypernyms());

            List<String> temp = new ArrayList<>();
            dBpediaAbstract.getSpotlightAnnotation().getResources().forEach(resource -> {
                temp.addAll(hypernyms.getOrDefault(resource.getUri(), new ArrayList<>()));
            });
            dBpediaAbstract.setHypernyms(temp);
            System.out.println(dBpediaAbstract.getHypernyms());
            mongo.getDatastore().save(dBpediaAbstract);
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("One by one: " + duration/1000);
    }

}
