# synthea-api

This project presents a REST API to use synthea as a service.

to build, ensure you have:

* jdk 8
* maven
* an editor (of your choice)

then, to build, run:

```mvn clean compile assembly:single```

to run the service locally, run:

```java -jar target/SyntheaAPI-1.0-SNAPSHOT-jar-with-dependencies.jar org.adghealth.SyntheaAPI```