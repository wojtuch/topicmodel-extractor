package org.dbpedia.topics;

import org.dbpedia.topics.dataset.models.Instance;
import org.dbpedia.topics.dataset.models.impl.BBCArticle;
import org.dbpedia.topics.dataset.models.impl.DBpediaAbstract;
import org.dbpedia.topics.io.MongoWrapper;
import org.dbpedia.topics.pipeline.impl.FindLemmasTask;
import org.dbpedia.topics.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wlu on 15.06.16.
 */
public class Testing {
    public static void main(String[] args) {
        FindLemmasTask l = new FindLemmasTask();
        Instance i = new BBCArticle();
        i.setText("Spin the radio dial in the US and you are likely to find plenty of Spanish-language music. But what you will not find is much Spanish-language hip-hop. Hip-hop and rap are actually quite popular in the Spanish-speaking world, but local artists are having trouble marketing their work abroad. But now, a US company is bringing rap and hip-hop en espanol to computer users everywhere. Los Caballeros de Plan G are one of Mexico's hottest hip-hop acts. They have a devoted fan base in their native Monterrey. But most Mexican hip-hop fans, not to mention fans in most of the Spanish-speaking world, rarely get a chance to hear the group's tracks on the radio. \"You can't really just go on the radio and listen to hip-hop in Spanish... it's just not accessible,\" says Manuel Millan, a native of San Diego, California. \"It's really hard for the Spanish hip-hop scene to get into mainstream radio. You usually have a very commercialised sound and the groups are not really known around the country or around the world.\" Millan and two friends set out to change that - they wanted to make groups like Los Caballeros de Plan G accessible to fans globally. Mainstream radio stations were not going to play this kind of music, and starting their own broadcast station was economically impossible. So, Millan and his friends launched a website called latinohiphopradio.com. The name says it all: it is web-based radio, devoted to the hottest Spanish language rap and hip-hop tracks. The site, which is in both in English and Spanish, is meant to be easy to navigate. All the user has to do is download a media player. There are no DJs. It is just music streamed over the net for free. Suddenly, with the help of the website, Los Caballeros de Plan G are producing \"export quality\" rap. The web might be just the right medium for Spanish language hip-hop right now. The genre is in what Millan calls its \"infant stage\". But the production values are improving, and artists such as Argentina's Mustafa Yoda are pushing to make it better and better. Mustafa Yoda is currently one of the hottest tracks on latinohiphopradio.com. \"He's considered the Eminem of Argentina, and the Latin American hip-hop scene,\" Millan says. \"He really hasn't had that much exposure as far as anywhere in the world, but he's definitely the one to look out for as far as becoming the next big thing in the Spanish-speaking world.\" Currently, the Chilean group Makisa is also in latinohiphopradio.com's top 10, as is Cuban artist Papo Record. \"Every country's got it's own cultural differences and they try to put those into their own songs,\" Millan says. Latinohiphopradio.com has been up and running for a couple of months now. The site has listeners from across the Spanish speaking world. Right now, Mexico leads the way, accounting for about 50% of listeners. But web surfers in Spain are logging in as well - about 25% of the web station's traffic comes from there. That is not surprising as many consider Spain to be the leader in Spanish-language rap and hip-hop. Millan says that Spain is actually just behind the United States and France in terms of overall rap and hip-hop production. That might be changing, though, as more and more Latin American artists are finding audiences. But one Spaniard is still firmly in latinohiphopradio.com's top 10. His name is Tote King and Manuel Millan says that he is the hip-hop leader in Spain. On his track Uno Contra Veinte Emcees, or One Against 20 Emcees, Tote King shows he is well aware of that fact. \"It's basically him bragging that he's one of the best emcees in Spain right now,\" Millan says. \"And it's pretty much true. He has the tightest productions, and his rap flow is impeccable, it's amazing.\" Latinohiphopradio.com is hoping to expand in the coming year. Millan says they want to include more music and more news from the world of Spanish language hip-hop and rap. Clark Boyd is technology correspondent for The World, a BBC World Service and WGBH-Boston co-production.");
        l.processInstance(i);
        System.out.println(i.getLemmas());
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
