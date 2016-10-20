package cc.mallet.topics;

import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.dbpedia.topics.modelling.HLDAWrapper;
import org.dbpedia.topics.rdfencoder.IEncoder;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by wlu on 26.07.16.
 */
public class HierarchicalLdaUtils {
    private HierarchicalLDA hlda;
    private HLDAWrapper hldaWrapper;

    public HierarchicalLdaUtils(HLDAWrapper hlda) {
        this.hldaWrapper = hlda;
        this.hlda = hlda.getModel();
    }

    public String nodesAsString() {
        StringBuffer result = new StringBuffer();
        nodeAsString(hlda.rootNode, 0, result);
        return result.toString();
    }

    private void nodeAsString(HierarchicalLDA.NCRPNode node, int indent, StringBuffer result) {
        for (int i=0; i<indent; i++) {
            result.append("  ");
        }

        result.append(node.totalTokens + "/" + node.customers + " ");
        result.append(node.getTopWords(hlda.numWordsToDisplay, false));
        result.append(System.lineSeparator());

        for (HierarchicalLDA.NCRPNode child : node.children) {
            nodeAsString(child, indent + 1, result);
        }
    }

    public int getNumLevels() {
        return this.hlda.numLevels;
    }

    public void addHierarchyToRDFModel(Model rdfModel) {
        addLevelToRDFModel(rdfModel, hlda.rootNode, null);
    }

    private void addLevelToRDFModel(Model rdfModel, HierarchicalLDA.NCRPNode node, HierarchicalLDA.NCRPNode parent) {
        IDSorter[] sortedTypes = new IDSorter[hlda.numTypes];

        for (int type=0; type < hlda.numTypes; type++) {
            sortedTypes[type] = new IDSorter(type, node.typeCounts[type]);
        }
        Arrays.sort(sortedTypes);

        Alphabet alphabet = hlda.instances.getDataAlphabet();

        Resource resource = rdfModel.getResource("Node"+node.totalTokens);

        String machineLabel = IntStream.range(0,4)
                .mapToObj(i -> (String)alphabet.lookupObject(sortedTypes[i].getID()))
                .map(term -> getSurfaceForm(term))
                .collect(Collectors.joining(" "));

        rdfModel.add(
                resource,
                rdfModel.createProperty(IEncoder.NAMESPACE+"hasMachineLabel"),
                rdfModel.createLiteral(machineLabel)
        );
        rdfModel.add(
                resource,
                rdfModel.createProperty(IEncoder.NAMESPACE+"hasHumanLabel"),
                rdfModel.createLiteral("")
        );
        rdfModel.add(
                resource,
                rdfModel.createProperty(IEncoder.NAMESPACE+"coversNumberOfDocuments"),
                rdfModel.createTypedLiteral(node.customers)
        );

        for (int i = 0; i < hlda.numWordsToDisplay; i++) {
//            if (withWeight){
//                out.append(alphabet.lookupObject(sortedTypes[i].getID()) + ":" + sortedTypes[i].getWeight() + " ");
//            }else
//            rdfModel.add(
//                    resource,
//                    rdfModel.createProperty("hasComponent"),
//                    rdfModel.createResource()
//            );

            Resource currentTopicComponent = rdfModel.createResource();

            rdfModel.add(
                    resource,
                    rdfModel.createProperty("hasTopicComponent"),
                    currentTopicComponent
            );

            String word = (String)alphabet.lookupObject(sortedTypes[i].getID());
            rdfModel.add(
                    currentTopicComponent,
                    rdfModel.createProperty(IEncoder.NAMESPACE+"hasWordComponent"),
                    rdfModel.createResource(word)
            );

            double weight = sortedTypes[i].getWeight();
            rdfModel.add(
                    currentTopicComponent,
                    rdfModel.createProperty(IEncoder.NAMESPACE+"hasProportion"),
                    rdfModel.createTypedLiteral(weight, XSDDatatype.XSDdouble)
            );
        }

        if (parent != null) {
            rdfModel.add(
                    resource,
                    rdfModel.createProperty(IEncoder.NAMESPACE+"hasSubclass"),
                    rdfModel.createResource("Node"+parent.totalTokens)
            );
            rdfModel.add(
                    rdfModel.createResource("Node"+parent.totalTokens),
                    rdfModel.createProperty(IEncoder.NAMESPACE+"isSubclassOf"),
                    resource
            );
        }

        for (HierarchicalLDA.NCRPNode child : node.children) {
            addLevelToRDFModel(rdfModel, child, node);
        }
    }

    private String getSurfaceForm (String term) {
        int idx = term.lastIndexOf("/");
        if (idx != -1) {
            if (idx == term.length() - 1) {
                term = term.substring(0, term.length()-1);
            }
            term = term.substring(idx+1);
        }
        return term;
    }
}
