package org.adghealth;

import java.util.Set;

import com.google.gson.Gson;

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
        return "The " + name + " abides.";
    }

    @GetMapping("/data")
    public String get_data() {
        Generator generator = new Generator();
        generator.run();
        return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
    }

    @PostMapping("/data")
    public String post_data(@RequestBody String body) {
        Gson gson = new Gson();
        SyntheaArguments syntheaArgs = gson.fromJson(body, SyntheaArguments.class);
        // Set properties from arguments
        Set<String> keys = syntheaArgs.config.stringPropertyNames();
        for (String key : keys) {
            String value = syntheaArgs.config.getProperty(key);
            System.out.println("Key: " + key + " Value: " + value);
            Config.set(key, value);
        }
        Generator generator = new Generator(syntheaArgs.options);
        generator.run();
        // Clear properties from arguments
        for (String key : keys) {
            System.out.println("Removing config key: " + key);
            Config.remove(key);
        }
        return gson.toJson(new StandardResponse(StatusResponse.SUCCESS));
    }
}