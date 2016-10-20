package org.dbpedia.topics.modelling;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by wlu on 26.05.16.
 */
public interface ITopicModelWrapper {
    void saveToFile(String outputfile) throws IOException;
    void readFromFile(String inputfile) throws IOException, ClassNotFoundException;
}
