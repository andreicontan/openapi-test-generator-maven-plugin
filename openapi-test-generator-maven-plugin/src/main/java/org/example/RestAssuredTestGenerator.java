package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.Map;
import java.util.Set;

/**
 * Generates RestAssured test cases based on OpenAPI specifications.
 */
public class RestAssuredTestGenerator {

    private static final Set<String> METHODS_WITH_BODY = Set.of("POST", "PUT", "PATCH");
    private final Log log;

    public RestAssuredTestGenerator(Log log) {
        this.log = log;
    }

    /**
     * Generates a RestAssured test method for a given endpoint.
     *
     * @param httpMethod        HTTP method (GET, POST, PUT, PATCH, DELETE).
     * @param url               API endpoint URL.
     * @param operationId       Unique operation ID from OpenAPI spec.
     * @param requestBodySchema JSON schema of the request body (if applicable).
     * @return Generated Java test method as a String.
     */
    public String generateTest(String httpMethod, String url, String operationId, JsonNode requestBodySchema) {
        StringBuilder testCode = new StringBuilder();

        testCode.append("    @Test\n")
                .append("    public void ").append(operationId).append("() {\n")
                .append("        given()\n")
                .append("            .baseUri(BASE_URL)\n");

        if (needsBody(httpMethod) && requestBodySchema != null) {
            String jsonBody = generateJsonFromSchema(requestBodySchema);
            testCode.append("            .contentType(\"application/json\")\n")
                    .append("            .body(").append(jsonBody).append(")\n");
        }

        testCode.append("        .when()\n")
                .append("            .").append(httpMethod.toLowerCase()).append("(\"").append(url).append("\")\n")
                .append("        .then()\n")
                .append("            .statusCode(200);\n")
                .append("    }\n\n");

        return testCode.toString();
    }

    /**
     * Checks if the HTTP method requires a request body.
     */
    private boolean needsBody(String httpMethod) {

        System.out.println("---------- " + httpMethod + "-----------");
        System.out.println("---------- " + httpMethod + "-----------");
        return METHODS_WITH_BODY.contains(httpMethod.toUpperCase());

    }

    /**
     * Generates a sample JSON request body based on the OpenAPI schema.
     */
    private String generateJsonFromSchema(JsonNode schema) {
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            Map<String, Object> sampleData = JsonSchemaToSampleData.generateSampleData(schema);
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sampleData);
        } catch (Exception e) {
            log.error("Error generating JSON body from schema", e);
            json = "{}";
        }
        return json;
    }
}
