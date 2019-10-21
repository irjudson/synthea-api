package org.adghealth;

import java.util.Properties;
import java.util.Set;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;
import static spark.Spark.put;

import com.google.gson.Gson;

import org.apache.xpath.Arg;
import org.mitre.synthea.engine.Generator;
import org.mitre.synthea.export.Exporter;
import org.mitre.synthea.helpers.Config;

public class SyntheaAPI {
    public static void main(String[] args) {
        // final UserService userService = new UserServiceMapImpl();
        get("/hello", (request, response) -> "Hello, Synthea");

        get("/hello/:name", (request, response) -> {
            return "Hello: " + request.params(":name");
        });

        get("/data", (request, response) -> {
            Generator.GeneratorOptions options = new Generator.GeneratorOptions();
            Generator generator = new Generator(options);
            generator.run();
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
        });

        post("/data", (request, response) -> {
            response.type("application/json");
            Gson gson = new Gson();
            SyntheaArguments syntheaArgs = gson.fromJson(request.body(), SyntheaArguments.class);
            // Set properties from arguments
            Set<String> keys = syntheaArgs.config.stringPropertyNames();
            for(String key : keys) {
                Config.set(key, syntheaArgs.config.getProperty(key));
            }
            Generator generator = new Generator(syntheaArgs.options);
            generator.run();
            // Clear properties from arguments
            for(String key : keys) {
                Config.remove(key);
            }
            return gson.toJson(new StandardResponse(StatusResponse.SUCCESS));
        });

        // post("/users", (request, response) -> {
        //     response.type("application/json");

        //     User user = new Gson().fromJson(request.body(), User.class);
        //     userService.addUser(user);

        //     return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
        // });

        // get("/users", (request, response) -> {
        //     response.type("application/json");

        //     return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, new Gson().toJsonTree(userService.getUsers())));
        // });

        // get("/users/:id", (request, response) -> {
        //     response.type("application/json");

        //     return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, new Gson().toJsonTree(userService.getUser(request.params(":id")))));
        // });

        // put("/users/:id", (request, response) -> {
        //     response.type("application/json");

        //     User toEdit = new Gson().fromJson(request.body(), User.class);
        //     User editedUser = userService.editUser(toEdit);

        //     if (editedUser != null) {
        //         return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, new Gson().toJsonTree(editedUser)));
        //     } else {
        //         return new Gson().toJson(new StandardResponse(StatusResponse.ERROR, new Gson().toJson("User not found or error in edit")));
        //     }
        // });

        // delete("/users/:id", (request, response) -> {
        //     response.type("application/json");

        //     userService.deleteUser(request.params(":id"));
        //     return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, "user deleted"));
        // });

        // options("/users/:id", (request, response) -> {
        //     response.type("application/json");

        //     return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, (userService.userExist(request.params(":id"))) ? "User exists" : "User does not exists"));
        // });
    }
}