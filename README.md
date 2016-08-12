# topicmodel-extractor

A repository for the "Combining DBpedia and Topic Modeling" 2016 GSoC project.
It consists of three modules - _spotlight-connector_, _topic-modeller_ and _inference-service_:
* Spotlight-connector is a collection of utilities for querying SPARQL endpoints and using DBpedia Spotlight to annotate texts. Incoming responses from the Spotlight server are parsed and can be used as Java objects.
* Topic-modeller is the core of this project. It allows for mining topics (using LDA and hLDA algorithms) from DBpedia abstracts and Wikipedia corpus.
* Inference-service is a REST service that predicts the topic distribution for arbitrary texts using previously mined topic model.

For more information about these components, please have a look in their folders.
