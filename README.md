# synthea-api

This project presents a REST API to use synthea as a service.

to build, ensure you have:

* jdk 8
* maven
* an editor (of your choice)
* this deploys via a pipeline.

then, to build, run:

```mvn package```

to run the service locally, run:

```mvn spring-boot:run```

There are configuration arguments you can send when you POST to the API endpoint. Here is an example:

```
{
 "options" : {
   population : 100
 },
 "config" : {
	"exporter.hospital.fhir.export" : false,
	"exporter.practitioner.fhir.export" : false,
	"generate.database_type" : "none"
 },
 "storageArgs" : {
	storeInFHIRService : true
 }
}
```