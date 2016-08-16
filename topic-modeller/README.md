# topic-modeller

This is the key component of the "Combining DBpedia and Topic Modeling" 2016 GSoC project.

#### Before you start

In order to use this project, make sure you have the following components installed / accessible:

* Maven
* MongoDB for storing preprocessed DBpedia abstracts.
* _Optionally_ Elasticsearch for importing the annotated and enriched dataset and utilizing the power search engine.
* _Optionally_ A triple store (e.g. Virtuoso) to query for hypernyms. It is optional for two reasons however - you don't have to mine topics using hypernyms and if you decide to do so, you can (and actually _should_ -- it's much faster) go for in-memory annotations, where the triple files are read into main memory.

#### Building and running

* Build the project using `mvn package`. Two jars (fat, with dependencies and a thin one) can be found in `target` directory.

* Make sure to have a look at the `props.properties` configuration file.
Entries without &lt;default&gt; values must be valid if you want to use associated functionality.

```
#endpoint url of the dbpedia spotlight instance to use
spotlight_endpoint=http://spotlight.sztaki.hu:2222/rest/annotate

#sparql endpoint containing hypernyms (en.lhd.extension.2015-10.nt)
hypernyms_sparql_endpoint=<your endpoint with uploaded triples>
hypernyms_triple_file=<local file for in memory lookup>

#sparql endpoint containing types and categories triples
dbpedia_sparql_endpoint=http://dbpedia.org/sparql
types_triple_file=<local file (instance_types_en.ttl) for in memory lookup>
categories_triple_file=<local file (article_categories_en.ttl) for in memory lookup>

# Triple file containing DBpedia abstracts (long_abstracts_en.ttl)
abstracts_triple_file=<path>

# Path to folder containing wikipedia articles cleaned using WikiExtractor:
# https://github.com/attardi/wikiextractor
wiki_as_xml_folder=<path to enwiki-latest-pages-articles-cleaned>

#mongo db access
mongo_server=localhost
mongo_port=32768
mongo_dbname=gsoc

elastic_server=localhost
elastic_port=9300
elastic_index=gsoc

# LDA settings - how many threads should be started for the parallel version
# of the algorithm and how many iterations should the algorithm run
lda_num_threads=16
lda_num_iterations=1000
```

##### Preprocess the documents by starting the enrichment pipeline:

```
java -jar target/topic-modeller-0.1.jar -Xms4g -Xmx32g --preprocessing \\
   --in-memory --reader abstracts \\
   --tasks lemma annotate types categories hypernyms --finisher mongo [--no-texts]
```

* Start _preprocessing_, do it _in memory_
* Read the _abstracts_ (from the file specified in `props.properties`)
* Lemmatize, annotate (link entities using Spotlight), fetch types, categories and hypernyms for every annotated entity.
* Store the result in MongoDB
* Optionally, to save some space, one might store the documents in MongoDB without texts (if only DBpedia resources matter) by setting the flag `--no-texts`.
* `-Xm*` flags should be used when using `--in-memory` parameter (when reading the triples files into RAM).

##### Mine topic models

```
java -jar target/topic-modeller-0.1.jar -Xms4g -Xmx64g --topic-modelling \\
   --algorithm lda --num-topics 20 50 100 200 500 --features e t c h \\
   --output models-lda-abstracts
```

* Start _topic modelling_
* Run the _LDA algorithm_ using selected _features_ (here: entities, types, categories, hypernyms)
* Do it 5 times, for k=20,50,100,200,500
* Save the mined models to _models-lda-abstracts_ directory.

```
java -jar target/topic-modeller-0.1.jar -Xms4g -Xmx64g --topic-modelling \\
   --algorithm hlda --num-levels 2 --features e t c h \\
   --output models-hlda-abstracts
```

* Start _topic modelling_
* Run the _hLDA algorithm_ using selected _features_ (here: entities, types, categories, hypernyms)
* Do it for the depth of the hLDA tree equal to 2
* Save the mined models to _models-hlda-abstracts_ directory.

As for now, mining topics on the corpus of wikipedia articles crashes. This remains to be done.

##### Save encoded topics as RDF

```
java -jar target/topic-modeller-0.1.jar -Xms4g -Xmx64g --encode \\
   --input models-lda-abstracts/50-e-h.ser \\
   --output 50eh.nt [--output-format NT, RDF/XML,...]
```

* Represent topics as RDF
* Take given topic model as _input_
* Use the specified file as _output_
* Optionally, specify the _output format_ (default is NT)

##### Dump documents from MongoDB

```
java -jar target/topic-modeller-0.1.jar -Xms4g -Xmx64g --dump-mongo \\
   --reader abstracts --output abstracts-serialized [--chunk-size 250000]
```

* Read the _DBpedia abstracts_ stored in MongoDB
* Serialize them to the specified _output_ folder in chunks (of default size 250k)
* This can be used as the step preceding importing the data to Elasticsearch

##### Insert documents to Elasticsearch

```
java -jar target/topic-modeller-0.1.jar -Xms4g -Xmx64g --import-to-elastic \\
   --input abstracts-serialized
```

* Read the serialized _DBpedia abstracts_ from the _input_ file/folder
* Insert them to Elasticsearch to the index specified in `props.properties`
