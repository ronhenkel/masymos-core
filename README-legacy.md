# masymos-core

## Management System for Models and Simulations
MaSyMoS is a Neo4J based database for SBML and CellML Models as well as simulation descriptions in SED-ML format. In addition, MaSyMoS contains bio-ontologies that are most frequently used for the annotation of models, i.e., GO, ChEBI, SBO, KiSAO.

All data is represented as a Graph and can be queried using Cypher. A description of the underlying database is available from our latest publication in [Oxford DATABASE](http://www.ncbi.nlm.nih.gov/pubmed/25754863).

MaSyMoS consists of different parts: 
- masymos-core is explained here
- masymos-morre is the search extender providing a REST-API is described at https://github.com/ronhenkel/masymos-morre
- masymos-cli contains the comand line interface and is located at https://github.com/ronhenkel/masymos-cli

## Setup
MaSyMoS can be used as a standalone or as a plugin for the [Neo4J WebServer](https://neo4j.com/download/other-releases/)
If you want to use MaSyMoS, the first step is to clone it into your IDE (e.g. Eclipse) and define it as a Maven Project. You might want to fork this repository before. 

### Standalone
To create a standalone you need masymos-core and masymos-cli. After cloning both, let Maven do its magic loading all required dependencies. Afterwards you can create an executeable JAR file including all dependencies having the entry point de.unirostock.sems.masymos.main.MainExtractor. 

You will be able to use the following arguments:
- `-dbPath $yourDBPath` points the the path of your database, if no DB exists one will be created
- `-directory $yourModelPath` points to the directory containing SBML, CellML, Sed-ML files, also OWL files can be processed
- `-type $selectType` specifies the expected type of input SBML|CELLML|SEDML|OWL
- `-ontology $referenceName` is expected when type is OWL, used to define the ontology namespace
- `-quiet` rewires the stdOut to dev0, faster but no debugging
- `-noAnno`creates only the model structures but does not resolve and index model annotations
- `-annoOnly`creates only the annotation index

The arguments `-noAnno`and `-annoOnly`can not be used together, obviously. If `-annoOnly' is provided all other parameters except for `-dbPath` are ignored. 
Creating the annotation index is time consuming, it spawns severeal Threads, uses [MiriamWebService](https://www.ebi.ac.uk/miriam/main/mdb?section=ws) to resolve all annotations into URLs and subsequently downloads several hundred webpages for text extraction and index creation.

If you start de.unirostock.sems.masymos.main.MainIndex you can use the argument `-deleteIndex $bool` and the aforementioned `-dbPath $yourDBPath` to delete all created index structures. 

de.unirostock.sems.masymos.main.MainAnalyzer allows to play around with some customozed analyzers, for more information go [here](https://lucene.apache.org/core/6_4_1/core/org/apache/lucene/analysis/Analyzer.html?is-external=true). This is handy if you want to check how text and querys are processed.

If you want to query your index you will need to start de.unirostock.sems.masymos.main.MainQuery and provide the `-dbPath $yourDBPath`. Afterwards you have a variety of query types to choose from.

### Server
If you want to use MaSyMoS as a search server for models you will have to set up the server version. To do so, start by downloading [Neo4j](https://neo4j.com/download/other-releases/). All 3.x versions should run without code adaptation, however, if you are using a version different from 3.0.1 (as currently defined in the Maven POM-file), you might want to make sure to compile MaSyMoS with the same version as the downloaded server. Follow the Neo4J setupt instructions.

You will need the following MaSyMoS repositories:
- masymos-core (this one)
- [masymos-morre](https://github.com/ronhenkel/masymos-morre)

Next, use Maven to compile masymos-core without dependencies and do the same with masymos-morre. Copy the JAR for masymos-core into $Neo4jRoot/lib/ext (you may need to create it). Copy the JAR for masymos-more into $Neo4jRoot/plugins. Open neo4j.conf in $Neo4JRoot/conf and set:
- `dbms.active_database=$yourDBPath`
- `dbms.allow_format_migration=true` if you are using a server with a higher version then the one the DB was created with
- `dbms.security.auth_enabled=false` or configure the authentification
- `dbms.unmanaged_extension_classes=de.unirostock.morre.server.plugin=/morre` to allow MaSyMoS plugin to be loaded
- you might also want to uncomment `dbms.connector.http.address=0.0.0.0:7474`to accept non-localhost connections

You will also need some additional libraries for the server, easiest way is to create masymos-core and masymos-morre without dependencies and let Maven pack all neccessary libraries in a directory next to the generated JAR. However, the following JAR files need to be present in $Neo4JRoot/lib/ext:
```
activation-1.1.jar
aopalliance-1.0.jar
apache-jena-libs-3.0.0.pom
axis-1.4.jar
axis-jaxrpc-1.4.jar
axis-saaj-1.4.jar
axis-wsdl4j-1.5.1.jar
BFLog-1.3.3.jar
BFUtils-0.4.1.jar
biojava3-ontology-3.1.0.jar
BiVeS-CellML-1.6.2.jar
BiVeS-Core-1.6.8.jar
commons-cli-1.3.jar
commons-codec-1.9.jar
commons-csv-1.0.jar
commons-discovery-0.2.jar
commons-lang3-3.1.jar
commons-logging-1.1.3.jar
dom4j-1.6.1.jar
gson-2.2.2.jar
guava-18.0.jar
guice-4.0-beta.jar
guice-multibindings-4.0-beta.jar
httpclient-4.5.2.jar
httpclient-cache-4.2.5.jar
httpcore-4.4.4.jar
jackson-annotations-2.3.0.jar
jackson-core-2.2.1.jar
jackson-databind-2.3.3.jar
JavaEWAH-0.8.6.jar
javax.inject-1.jar
javax.ws.rs-api-2.0.jar
jaxen-1.1.1.jar
jcl-over-slf4j-1.7.7.jar
jCOMODI-0.5.9.jar
jdom-1.0.jar
jdom-1.1.3.jar
jdom-contrib-1.1.3.jar
jdom2-2.0.5.jar
jena-arq-3.0.0.jar
jena-base-3.0.0.jar
jena-core-3.0.0.jar
jena-iri-3.0.0.jar
jena-shaded-guava-3.0.0.jar
jena-tdb-3.0.0.jar
jfact-4.0.0.jar
jigsaw-2.2.6.jar
jlibsedml-2.0.0.jar
jmathml-2.1.0.jar
jms-1.1.jar
jmxri-1.2.1.jar
jmxtools-1.2.1.jar
joda-time-2.3.jar
jsbml-1.1-b1.jar
jsbml-arrays-1.1-b1.jar
jsbml-comp-1.1-b1.jar
jsbml-core-1.1-b1.jar
jsbml-distrib-1.1-b1.jar
jsbml-dyn-1.1-b1.jar
jsbml-fbc-1.1-b1.jar
jsbml-groups-1.1-b1.jar
jsbml-layout-1.1-b1.jar
jsbml-multi-1.1-b1.jar
jsbml-qual-1.1-b1.jar
jsbml-render-1.1-b1.jar
jsbml-req-1.1-b1.jar
jsbml-spatial-1.1-b1.jar
jsbml-tidy-1.1-b1.jar
json-simple-1.1.1.jar
jsonld-java-0.5.0.jar
jsonld-java-sesame-0.5.0.jar
jsoup-1.7.2.jar
jsr305-2.0.1.jar
jtidy-r938.jar
junit-3.8.1.jar
libthrift-0.9.2.jar
log4j-1.2.15.jar
lucene-backward-codecs-5.5.0.jar
mail-1.4.jar
masymos-core-0.9.0.jar
metainf-services-1.1.jar
miriam-lib-1.1.6.jar
org.apache.commons.io-2.4.jar
owlapi-api-4.0.2.jar
owlapi-distribution-4.0.2.jar
saxon-8.7.jar
Saxon-B-9.0.jar
saxon-dom-8.7.jar
sbgn-SEMS-2.jar
semargl-core-0.6.1.jar
semargl-rdf-0.6.1.jar
semargl-rdfa-0.6.1.jar
semargl-sesame-0.6.1.jar
sesame-model-2.7.12.jar
sesame-rio-api-2.7.12.jar
sesame-rio-binary-2.7.12.jar
sesame-rio-datatypes-2.7.12.jar
sesame-rio-languages-2.7.12.jar
sesame-rio-n3-2.7.12.jar
sesame-rio-nquads-2.7.12.jar
sesame-rio-ntriples-2.7.12.jar
sesame-rio-rdfjson-2.7.12.jar
sesame-rio-rdfxml-2.7.12.jar
sesame-rio-trig-2.7.12.jar
sesame-rio-trix-2.7.12.jar
sesame-rio-turtle-2.7.12.jar
sesame-util-2.7.12.jar
slf4j-log4j12-1.7.12.jar
spi-0.2.4.jar
stax2-api-3.1.4.jar
staxmate-2.3.0.jar
trove4j-3.0.3.jar
woodstox-core-5.0.1.jar
xalan-2.7.0.jar
xercesImpl-2.8.0.jar
xml-apis-1.3.03.jar
xmlutils-0.6.6.jar
xom-1.2.5.jar
xpp3_min-1.1.4c.jar
xstream-1.3.1.jar
xz-1.5.jar
```

Finally, we need to add $Neo4JRoot/lib/ext to the servers classpath. Open $Neo4JRoot/bin/Neo4j-Management/Get-Java.ps1 and look for a line like
```
# Build the Java command line
      $ClassPath="$($Neo4jServer.Home)/lib/*;$($Neo4jServer.Home)/plugins/*"
```
and add `;$($Neo4jServer.Home)/lib/ext/*`

Well done, you can now start MaSyMos! Please refer to masymos-morre to read about [how to work with the server](https://github.com/ronhenkel/masymos-morre).
