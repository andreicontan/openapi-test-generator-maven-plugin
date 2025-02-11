package org.example;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class to generate sample JSON data based on an OpenAPI JSON schema.
 */
public class JsonSchemaToSampleData {

    /**
     * Generates sample JSON data from a given schema.
     *
     * @param schema JSON schema node
     * @return Map representation of the JSON data
     */
    public static Map<String, Object> generateSampleData(JsonNode schema) {
        Map<String, Object> sampleData = new HashMap<>();

        if (schema.has("properties")) {
            JsonNode properties = schema.get("properties");
            Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode fieldSchema = field.getValue();
                sampleData.put(key, generateSampleValue(fieldSchema));
            }
        }
        return sampleData;
    }

    /**
     * Generates a sample value based on a given field schema.
     */
    private static Object generateSampleValue(JsonNode fieldSchema) {
        if (fieldSchema.has("type")) {
            String type = fieldSchema.get("type").asText();
            return switch (type) {
                case "string" -> "sample_string";
                case "integer" -> 123;
                case "number" -> 123.45;
                case "boolean" -> true;
                case "array" -> List.of(generateSampleValue(fieldSchema.get("items")));
                case "object" -> generateSampleData(fieldSchema);
                default -> "unknown_value";
            };
        }
        return "unknown_value";
    }
}
