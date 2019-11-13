package org.adghealth;

import java.util.List;
import java.util.Set;
import java.io.*;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;

import com.google.gson.Gson;
import com.microsoft.azure.storage.CloudStorageAccount;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.mitre.synthea.engine.Generator;
import org.mitre.synthea.helpers.Config;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class APIController {

    @GetMapping("/")
    public String index() {
        return "Hello World!";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Synthea!";
    }

    @GetMapping("/hello/{name}")
    public String hello_name(@PathVariable String name) {
        if (name == "dude") {
            return "The " + name + " abides.";
        } else {
            return "Hello, " + name + "!";
        }
    }

    @GetMapping("/data")
    public String get_data() {
        Generator generator = new Generator();
        generator.run();
        cleanUpFiles();
        return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
    }

    @PostMapping("/data")
    public String post_data(@RequestBody String body) {
        Gson gson = new Gson();
        SyntheaArguments syntheaArgs = gson.fromJson(body, SyntheaArguments.class);
        
        Set<String> keys = processParameters(syntheaArgs);
        
        Generator generator = new Generator(syntheaArgs.options);
        generator.run();

        pushFiles(syntheaArgs);
        cleanupConfig(keys);
        cleanUpFiles();

        return gson.toJson(new StandardResponse(StatusResponse.SUCCESS));
    }

    private void cleanUpFiles() {
        // Delete files
        File source = new File("./output/fhir");
        List<File> files = (List<File>) FileUtils.listFiles(source, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        for (File file : files) {
            file.delete();
        }
    }

    private void cleanupConfig(Set<String> keys) {
        // Clear properties from arguments
        for (String key : keys) {
            System.out.println("Removing config key: " + key);
            Config.remove(key);
        }
    }

    private Set<String> processParameters(SyntheaArguments syntheaArgs) {
        // Set properties from arguments
        Set<String> keys = syntheaArgs.config.stringPropertyNames();
        for (String key : keys) {
            String value = syntheaArgs.config.getProperty(key);
            System.out.println("Key: " + key + " Value: " + value);
            Config.set(key, value);
        }
        return keys;
    }

    private void pushFiles(SyntheaArguments syntheaArgs) {
        CloudStorageAccount storageAccount;
        CloudBlobClient blobClient = null;
        CloudBlobContainer container = null;

        if (syntheaArgs.storageArgs.storeInFHIRService) {
            try {
                // Move data to blob specified
                String storageConnectionString = System.getenv("FHIRBlobConnectionString");
                if (syntheaArgs.storageArgs.connectionString != null) {
                    storageConnectionString = syntheaArgs.storageArgs.connectionString;
                }
                System.out.println("Using connection string: " + storageConnectionString);
                String containerName = System.getenv("FHIRImportContainerName");
                if (syntheaArgs.storageArgs.containerName != null) {
                    containerName = syntheaArgs.storageArgs.containerName;
                }
                System.out.println("Using container name: " + containerName);

                storageAccount = CloudStorageAccount.parse(storageConnectionString);
                blobClient = storageAccount.createCloudBlobClient();
                container = blobClient.getContainerReference(containerName);

                File source = new File("./output/fhir");
                List<File> files = (List<File>) FileUtils.listFiles(source, TrueFileFilter.INSTANCE,
                        TrueFileFilter.INSTANCE);
                for (File file : files) {
                    // Getting a blob reference
                    CloudBlockBlob blob = container.getBlockBlobReference(file.getName());
                    // Creating blob and uploading file to it
                    System.out.println("Uploading file: " + file.getName());
                    blob.uploadFromFile(file.getAbsolutePath());
                }

            } catch (StorageException ex) {
                System.out.println(String.format("Error returned from the service. Http code: %d and error code: %s",
                        ex.getHttpStatusCode(), ex.getErrorCode()));
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}