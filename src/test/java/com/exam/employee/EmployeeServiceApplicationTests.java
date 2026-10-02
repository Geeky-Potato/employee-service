package com.exam.employee;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import tools.jackson.databind.json.JsonMapper;

class EmployeeServiceApplicationTests {

    static final String MOHAMED = """
            {"firstName":"Mohamed","lastName":"Ahmed","dateOfBirth":"2000-01-01","salary":"1000","joinDate":"2023-05-18","department":"IT"}""";
    static final String SALMAN = """
            {"firstName":"Salman","lastName":"Ebrahim","dateOfBirth":"1990-01-01","salary":"2000","joinDate":"2016-03-18","department":"Business"}""";

    final HttpClient http = HttpClient.newHttpClient();
    String url;

    @Test
    void employeesApi() throws Exception {
        Path file = Path.of("target", "test-employees.json");
        Files.deleteIfExists(file);
        try (var app = SpringApplication.run(EmployeeServiceApplication.class, "--server.port=0", "--employees.file=" + file)) {
            url = "http://localhost:" + app.getEnvironment().getProperty("local.server.port") + "/employees";

            var created = send("", MOHAMED);
            assertEquals(201, created.statusCode());
            assertEquals("{\"id\":1}", created.body());
            assertEquals("{\"id\":2}", send("", SALMAN).body());
            assertEquals(400, send("", MOHAMED.replace("\"firstName\":\"Mohamed\",", "")).statusCode());

            var mohamed = send("/1", null);
            assertEquals(200, mohamed.statusCode());
            assertEquals("{\"id\":1," + MOHAMED.substring(1), mohamed.body());
            assertEquals(404, send("/99", null).statusCode());
            assertEquals(List.of(1L, 2L), ids(""));
            assertEquals(List.of(2L), ids("?name=ebra"));
            assertEquals(List.of(1L), ids("?name=moh"));
            assertEquals(List.of(2L), ids("?fromSalary=1500"));
            assertEquals(List.of(1L), ids("?toSalary=1500"));
            assertEquals(List.of(1L, 2L), ids("?fromSalary=1000&toSalary=2000"));
            assertEquals(List.of(2L), ids("?name=a&fromSalary=1001&toSalary=2000"));
        }
    }

    HttpResponse<String> send(String path, String json) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(url + path));
        if (json != null) {
            request.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json));
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    List<Long> ids(String query) throws Exception {
        return Stream.of(new JsonMapper().readValue(send(query, null).body(), Employee[].class)).map(e -> e.id()).toList();
    }
}
