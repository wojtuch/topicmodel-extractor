package org.dbpedia.topics.rdfencoder;

import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.dbpedia.topics.modelling.LDAWrapper;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by wlu on 05.07.16.
 */
public class LDA2RDFEncoder implements IEncoder {
    private Model rdfModel;
    private LDAWrapper ldaModel;

    public LDA2RDFEncoder(LDAWrapper ldaModel) {
        this.ldaModel = ldaModel;

        String graphName = String.format("%d-%s",
                ldaModel.getModel().getNumTopics(),
                ldaModel.getFeatures().stream().collect(Collectors.joining("-")));
        rdfModel = ModelFactory.createMemModelMaker().createModel(graphName);
    }

    @Override
    public void encodeTopics(int numTopicDescribingWords) {
        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = ldaModel.getModel().getAlphabet();

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = ldaModel.getModel().getSortedWords();

        // Show top words in topics with proportions for the first document
        for (int topic = 0; topic < ldaModel.getModel().getNumTopics(); topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

            int rank = 0;
            List<String> topicWords = new ArrayList<>();
            List<Double> topicWordsWeights = new ArrayList<>();

            while (iterator.hasNext() && rank < numTopicDescribingWords) {
                IDSorter idCountPair = iterator.next();
                topicWords.add((String) dataAlphabet.lookupObject(idCountPair.getID()));
                topicWordsWeights.add(idCountPair.getWeight());
                rank++;
            }

            double sumWeights = topicSortedWords.get(topic).stream().mapToDouble(ids -> ids.getWeight()).sum();

            topicWordsWeights = topicWordsWeights.stream()
                    .map(wt -> new BigDecimal(wt*100/sumWeights).setScale(2, RoundingMode.HALF_UP).doubleValue())
                    .collect(Collectors.toList());

            String topicLabel = "Topic"+(topic+1);
            String topicUri = NAMESPACE+topicLabel;

            Resource resource = rdfModel.getResource(topicUri);

            rdfModel.add(
                resource,
                rdfModel.createProperty(NAMESPACE_RDF+"type"),
                rdfModel.createResource(NAMESPACE_DBO+"topicModel")
            );

            for (int i = 0; i < topicWords.size(); i++) {
                Resource currentTopicComponent = rdfModel.createResource();

                rdfModel.add(
                        resource,
                        rdfModel.createProperty(NAMESPACE+"hasTopicComponent"),
                        currentTopicComponent
                );

                String word = topicWords.get(i);
                rdfModel.add(
                        currentTopicComponent,
                        rdfModel.createProperty(NAMESPACE+"hasWordComponent"),
                        rdfModel.createResource(word)
                );

                Double proportion = topicWordsWeights.get(i);
                rdfModel.add(
                        currentTopicComponent,
                        rdfModel.createProperty(NAMESPACE+"hasProportion"),
                        rdfModel.createTypedLiteral(proportion, XSDDatatype.XSDdouble)
                );
            }
        }
    }

    @Override
    public void encodeOneObservation(String uri, String text) {

        double[] probabilities = ldaModel.predict(text);
        for (int i = 0; i < probabilities.length; i++) {
            String topicLabel = "Topic"+(i+1);
            double probability = new BigDecimal(probabilities[i]*100).setScale(2, RoundingMode.HALF_UP).doubleValue();

            Resource anonRes = rdfModel.createResource();
            rdfModel.add(
                    anonRes,
                    rdfModel.createProperty(NAMESPACE_RDF+"type"),
                    rdfModel.createResource(NAMESPACE+"topicObservation")
            );

            rdfModel.add(
                    anonRes,
                    rdfModel.createProperty(NAMESPACE+"hasEntityComponent"),
                    rdfModel.getResource(uri)
            );

            rdfModel.add(
                    anonRes,
                    rdfModel.createProperty(NAMESPACE+"hasTopicComponent"),
                    rdfModel.getResource(NAMESPACE+topicLabel)
            );

            rdfModel.add(
                    anonRes,
                    rdfModel.createProperty(NAMESPACE+"hasProportion"),
                    rdfModel.createTypedLiteral(probability, XSDDatatype.XSDdouble)
            );
        }
    }

    public String toString(String format) {
        StringWriter out = new StringWriter();
        rdfModel.write(out, format);
        return out.toString();
    }
}
