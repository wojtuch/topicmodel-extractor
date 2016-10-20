package org.dbpedia.topics.rdfencoder;

/**
 * Created by wlu on 20.10.16.
 */
public interface IEncoder {
    String NAMESPACE = "http://example.org/topic-vocab#";
    String NAMESPACE_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    String NAMESPACE_DBO = "http://dbpedia.org/ontology/";

    void encodeTopics(int numTopicDescribingWords);
    void encodeOneObservation(String uri, String text);
    String toString(String outputFormat);
}
