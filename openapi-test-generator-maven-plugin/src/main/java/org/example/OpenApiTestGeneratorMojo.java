package org.example;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * Maven Plugin for generating JUnit test cases from an OpenAPI 3.0 spec.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, requiresProject = false)
public class OpenApiTestGeneratorMojo extends AbstractMojo {

    @Parameter(property = "openapi.inputFile", required = true)
    private String inputFile;

    @Parameter(property = "openapi.outputFile", defaultValue = "${project.build.directory}/GeneratedApiTests.java")
    private String outputFile;

    public void execute() throws MojoExecutionException {
        getLog().info("Reading OpenAPI spec: " + inputFile);

        OpenAPI openAPI = new OpenAPIV3Parser().read(inputFile);
        if (openAPI == null) {
            throw new MojoExecutionException("Failed to parse OpenAPI file.");
        }

        StringBuilder testClass = new StringBuilder();
        testClass.append("import org.junit.jupiter.api.Test;\n")
                .append("import static io.restassured.RestAssured.*;\n")
                .append("import static org.hamcrest.Matchers.*;\n")
                .append("import io.restassured.http.ContentType;\n\n")
                .append("public class GeneratedApiTests {\n");

        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            String path = entry.getKey();
            for (Map.Entry<PathItem.HttpMethod, Operation> methodEntry : entry.getValue().readOperationsMap().entrySet()) {
                PathItem.HttpMethod httpMethod = methodEntry.getKey();
                Operation operation = methodEntry.getValue();

                // Handle missing operationId
                String operationName = operation.getOperationId();
                if (operationName == null || operationName.isEmpty()) {
                    operationName = path.replaceAll("[^a-zA-Z0-9]", "_");
                }
                operationName = httpMethod.name().toUpperCase();


                // Check if request body is defined
                boolean hasRequestBody = operation.getRequestBody() != null
                        && operation.getRequestBody().getContent() != null;

                for (Map.Entry<String, io.swagger.v3.oas.models.responses.ApiResponse> response : operation.getResponses().entrySet()) {
                    String statusCode = response.getKey();

                    testClass.append("\t@Test\n")
                            .append("\tvoid test_").append(operationName).append("_").append(statusCode).append("() {\n")
                            .append("\t\tgiven()\n");

                    // Only add body if request body is actually defined
                    if (hasRequestBody && (operationName.equalsIgnoreCase("post")
                            || operationName.equalsIgnoreCase("put")
                            || operationName.equalsIgnoreCase("patch"))) {
                        System.out.println("+++++++" + operationName + "+++++++");

                        String sampleRequestBody = generateSampleRequestBody(operation);
                        // Ensure valid body
                        testClass.append("\t\t\t.contentType(ContentType.JSON)\n")
                                .append("\t\t\t.body(").append(sampleRequestBody).append(")\n");

                    }

                    testClass.append("\t\t.when()\n")
                            .append("\t\t\t.request(\"").append(httpMethod.name()).append("\", \"").append(path).append("\")\n")
                            .append("\t\t.then()\n")
                            .append("\t\t\t.statusCode(").append(statusCode).append(");\n")
                            .append("\t}\n\n");
                }
            }
        }

        testClass.append("}\n");

        try {
            Files.write(Path.of(outputFile), testClass.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            getLog().info("Test cases generated at: " + outputFile);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write test file", e);
        }
    }

    /**
     * Generates a sample JSON request body based on OpenAPI schema.
     */
    private String generateSampleRequestBody(Operation operation) {
        if (operation.getRequestBody() == null || operation.getRequestBody().getContent() == null) {
            return "\"{}\""; // Return empty JSON if no body is defined
        }

        Content content = operation.getRequestBody().getContent();
        for (Map.Entry<String, MediaType> entry : content.entrySet()) {
            MediaType mediaType = entry.getValue();
            if (mediaType.getSchema() != null) {
                return "\"" + generateSampleJson(mediaType.getSchema()) + "\"";
            }
        }
        return "\"{}\"";
    }

    /**
     * Recursively generates a sample JSON string from an OpenAPI schema.
     */
    private String generateSampleJson(Schema<?> schema) {
        if (schema.getType() == null) {
            return "{}";
        }

        switch (schema.getType()) {
            case "string":
                return "example_string";
            case "integer":
                return "123";
            case "boolean":
                return "true";
            case "object":
                StringBuilder json = new StringBuilder("{");
                if (schema.getProperties() != null) {
                    boolean first = true;
                    for (Map.Entry<String, Schema> entry : schema.getProperties().entrySet()) {
                        if (!first) {
                            json.append(", ");
                        }
                        json.append("\"").append(entry.getKey()).append("\": ")
                                .append("\"").append(generateSampleJson(entry.getValue())).append("\"");
                        first = false;
                    }
                }
                json.append("}");
                return json.toString();
            case "array":
                if (schema.getItems() != null) {
                    return "[" + generateSampleJson(schema.getItems()) + "]";
                }
                return "[]";
            default:
                return "{}";
        }
    }
}
