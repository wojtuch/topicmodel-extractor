package org.dbpedia.topics.rdfencoder;

import cc.mallet.topics.HierarchicalLdaUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dbpedia.topics.modelling.HLDAWrapper;

import java.io.StringWriter;
import java.util.stream.Collectors;

/**
 * Created by wlu on 05.07.16.
 */
public class HLDA2RDFEncoder implements IEncoder {
    private Model rdfModel;
    private HLDAWrapper hldaModel;
    private HierarchicalLdaUtils hierarchicalLdaUtils;

    public HLDA2RDFEncoder(HLDAWrapper hldaModel) {
        this.hldaModel = hldaModel;

        String graphName = String.format("%s",
//                this.hierarchicalLdaUtils.getNumLevels(),
                this.hldaModel.getFeatures().stream().collect(Collectors.joining("-")));
        rdfModel = ModelFactory.createMemModelMaker().createModel(graphName);
    }

    @Override
    public void encodeTopics(int numTopicDescribingWords) {
        this.hierarchicalLdaUtils = new HierarchicalLdaUtils(this.hldaModel);
        this.hierarchicalLdaUtils.addHierarchyToRDFModel(this.rdfModel);
    }

    @Override
    public void encodeOneObservation(String uri, String text) {
        double[] probabilities = hldaModel.predict(text);
    }

    public String toString(String format) {
        StringWriter out = new StringWriter();
        rdfModel.write(out, format);
        return out.toString();
    }
}
