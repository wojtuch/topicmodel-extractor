# inference-service

The REST backend used in the demonstration part of the "Combining DBpedia and Topic Modeling" 2016 GSoC project.

#### Building and running

* Build the project using `mvn package`. Two jars (fat, with dependencies and a thin one) can be found in `target` directory.

* Make sure to have a look at the `inferencer.properties` configuration file.
Entries without &lt;default&gt; values must be valid if you want to use associated functionality.

```
# Base URI the Grizzly HTTP server will listen on
server_base_uri=http://0.0.0.0:8182/

# how many words should be used to describe a topic
num_topic_words=15

# where are the triple files
hypernyms_triple_file=/media/data/datasets/gsoc/hypernyms/en.lhd.extension.2015-10.nt
types_triple_file=/media/data/datasets/gsoc/instance_types_en.ttl
categories_triple_file=/media/data/datasets/gsoc/article_categories_en.ttl

cache_directory=inferencer-cache
cache_file_words_for_topics=wordsForTopics.ser
cache_file_word_coverages_for_topics=wordCoveragesForTopics.ser
cache_file_topics_labels=topicsLabels.txt
```

##### Starting the server

```
java -jar target/inference-service-0.1.jar \\
   --model-file ../topic-modeller/models-lda-abstracts/50-e-h-t.ser --in-memory \\
   [--categories /media/data/datasets/gsoc/article_categories_en.ttl \\
   --types /media/data/datasets/gsoc/instance_types_en.ttl \\
   --hypernyms /media/data/datasets/gsoc/hypernyms/en.lhd.extension.2015-10.nt]

```

* Start the service and load the specified topic model
* Load the triples to RAM (either specify the files here or in `inferencer.properties`).

##### Using the service

* The service accepts the POST requests:
```
curl -H "Accept: application/json" -X POST \\
    -d '{"spotlightAnnotationJSON":"{"@text":"First documented in the 13th century, Berlin was the capital of the Kingdom of Prussia (1701–1918), the German Empire (1871–1918), the Weimar Republic (1919–33) and the Third Reich (1933–45). Berlin in the 1920s was the third largest municipality in the world. After World War II, the city became divided into East Berlin -- the capital of East Germany -- and West Berlin, a West German exclave surrounded by the Berlin Wall from 1961–89. Following German reunification in 1990, the city regained its status as the capital of Germany, hosting 147 foreign embassies.","@confidence":"0.5","@support":"0","@types":"","@sparql":"","@policy":"whitelist","Resources":[{"@URI":"http://dbpedia.org/resource/Berlin","@support":"46739","@types":"Schema:Place,DBpedia:Place,DBpedia:PopulatedPlace,DBpedia:Region,Schema:AdministrativeArea,DBpedia:AdministrativeRegion","@surfaceForm":"Berlin","@offset":"38","@similarityScore":"0.9998758570579835","@percentageOfSecondRank":"6.324471425147983E-5"},{"@URI":"http://dbpedia.org/resource/Kingdom_of_Prussia","@support":"5158","@types":"Schema:Place,DBpedia:Place,DBpedia:PopulatedPlace,Schema:Country,DBpedia:Country","@surfaceForm":"Prussia","@offset":"79","@similarityScore":"0.7819490774521395","@percentageOfSecondRank":"0.2629082834645425"},{"@URI":"http://dbpedia.org/resource/German_Empire","@support":"11935","@types":"Schema:Place,DBpedia:Place,DBpedia:PopulatedPlace,Schema:Country,DBpedia:Country","@surfaceForm":"German Empire","@offset":"104","@similarityScore":"0.9870307789816448","@percentageOfSecondRank":"0.0071095284503395716"},{"@URI":"http://dbpedia.org/resource/Weimar_Republic","@support":"3328","@types":"Schema:Place,DBpedia:Place,DBpedia:PopulatedPlace,Schema:Country,DBpedia:Country","@surfaceForm":"Weimar Republic","@offset":"135","@similarityScore":"0.9999999993985398","@percentageOfSecondRank":"5.96802983833605E-10"},{"@URI":"http://dbpedia.org/resource/Nazi_Germany","@support":"28496","@types":"Schema:Place,DBpedia:Place,DBpedia:PopulatedPlace,Schema:Country,DBpedia:Country","@surfaceForm":"Third Reich","@offset":"169","@similarityScore":"0.999970027925537","@percentageOfSecondRank":"2.8783824766873777E-5"},{"@URI":"http://dbpedia.org/resource/Berlin","@support":"46739","@types":"Schema:Place,DBpedia:Place,DBpedia:PopulatedPlace,DBpedia:Region,Schema:AdministrativeArea,DBpedia:AdministrativeRegion","@surfaceForm":"Berlin","@offset":"192","@similarityScore":"0.9998758570579835","@percentageOfSecondRank":"6.324471425147983E-5"},{"@URI":"http://dbpedia.org/resource/Municipalities_of_Germany","@support":"8098","@types":"","@surfaceForm":"municipality","@offset":"234","@similarityScore":"0.8087650517513935","@percentageOfSecondRank":"0.22978871403858298"},{"@URI":"http://dbpedia.org/resource/World_War_II","@support":"163937","@types":"","@surfaceForm":"World War II","@offset":"267","@similarityScore":"0.9999691163624151","@percentageOfSecondRank":"2.2825526578918066E-5"},{"@URI":"http://dbpedia.org/resource/East_Berlin","@support":"1688","@types":"Schema:Place,DBpedia:Place,DBpedia:PopulatedPlace,DBpedia:Region,Schema:AdministrativeArea,DBpedia:AdministrativeRegion","@surfaceForm":"East Berlin","@offset":"310","@similarityScore":"0.9999980129473289","@percentageOfSecondRank":"1.987056612783919E-6"},{"@URI":"http://dbpedia.org/resource/East_Germany","@support":"10457","@types":"Schema:Place,DBpedia:Place,DBpedia:PopulatedPlace,Schema:Country,DBpedia:Country","@surfaceForm":"East Germany","@offset":"340","@similarityScore":"0.9999999932052219","@percentageOfSecondRank":"6.794780107670407E-9"},{"@URI":"http://dbpedia.org/resource/West_Berlin","@support":"2258","@types":"Schema:Place,DBpedia:Place,DBpedia:PopulatedPlace,DBpedia:Region,Schema:AdministrativeArea,DBpedia:AdministrativeRegion","@surfaceForm":"West Berlin","@offset":"360","@similarityScore":"0.9999984137182953","@percentageOfSecondRank":"1.5862842176286193E-6"},{"@URI":"http://dbpedia.org/resource/West_Germany","@support":"9065","@types":"Schema:Place,DBpedia:Place,DBpedia:PopulatedPlace,Schema:Country,DBpedia:Country","@surfaceForm":"West German","@offset":"375","@similarityScore":"0.9996413242882213","@percentageOfSecondRank":"3.5874521388146836E-4"},{"@URI":"http://dbpedia.org/resource/Enclave_and_exclave","@support":"1984","@types":"","@surfaceForm":"exclave","@offset":"387","@similarityScore":"1.0","@percentageOfSecondRank":"1.0646119271806581E-20"},{"@URI":"http://dbpedia.org/resource/Berlin_Wall","@support":"2229","@types":"Schema:Place,DBpedia:Place,DBpedia:ArchitecturalStructure,DBpedia:Building","@surfaceForm":"Berlin Wall","@offset":"413","@similarityScore":"0.9999999999996589","@percentageOfSecondRank":"3.165311358261024E-13"},{"@URI":"http://dbpedia.org/resource/German_reunification","@support":"1989","@types":"","@surfaceForm":"German reunification","@offset":"449","@similarityScore":"0.9999997861474641","@percentageOfSecondRank":"1.5374345655254399E-7"},{"@URI":"http://dbpedia.org/resource/Weimar_Republic","@support":"3328","@types":"Schema:Place,DBpedia:Place,DBpedia:PopulatedPlace,Schema:Country,DBpedia:Country","@surfaceForm":"Germany","@offset":"526","@similarityScore":"0.8151587420656723","@percentageOfSecondRank":"0.2098942527082445"}]}"}' \\
    http://localhost:8183/inference-service/get-topics
```

* One only needs to send the JSON response from Spotlight annotation as parameter `spotlightAnnotationJSON`.

* The service answers a JSON in following format:

```
{ "predictions": [
  {
   "topicId": Integer,
   "topicProbability": Double,
   "topicWords": String array
    [
      "word_1", "word_2", ..., "word_N"
    ],
    "topicWordsCoverage": Double array
     [
      "coverage_word_1", "coverage_word_2", ..., "coverage_word_N"
     ]
  },
  ...
 ],
 "status": String
}
```

* There is possibility for giving the topics meaningful names manually.
In order to do so, one has to create an empty file and write labels one under another.
And so, string in the kth line will be the label of Topic k.
The service will search for the file in the following location (for a model from previous example and the default cache directory) `inferencer-cache/50-e-h-t/topicsLabels.txt`.

* For a version of DBpedia Spotlight demo interface with enabled support of the inference service, check out the [following fork](https://github.com/wojtuch/demo).
