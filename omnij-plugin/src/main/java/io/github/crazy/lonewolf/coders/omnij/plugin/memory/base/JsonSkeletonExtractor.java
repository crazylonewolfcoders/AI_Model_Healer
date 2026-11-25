package io.github.crazy.lonewolf.coders.omnij.plugin.memory.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public final class JsonSkeletonExtractor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String extractSafeSkeleton(String rawJson) {
        try {
            JsonNode root = MAPPER.readTree(rawJson);
            JsonNode sanitized = sanitizeNode(root);
            return MAPPER.writeValueAsString(sanitized);
        } catch (Exception e) {
            return "{}"; // Fallback if malformed JSON arrived
        }
    }

    private static JsonNode sanitizeNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return TextNode.valueOf("NULL");
        }
        if (node.isObject()) {
            ObjectNode out = MAPPER.createObjectNode();
            node.fields().forEachRemaining(entry -> {
                String safeKey = sanitizeKey(entry.getKey());
                out.set(safeKey, sanitizeNode(entry.getValue()));
            });
            return out;
        }
        if (node.isArray()) {
            ArrayNode out = MAPPER.createArrayNode();
            if (!node.isEmpty()) {
                // Collapse array to 1 representative element (merging keys if elements vary)
                ObjectNode mergedObject = null;
                for (JsonNode item : node) {
                    if (item.isObject()) {
                        if (mergedObject == null) mergedObject = MAPPER.createObjectNode();
                        JsonNode sanitizedItem = sanitizeNode(item);
                        ObjectNode finalMergedObject = mergedObject;
                        sanitizedItem.fields().forEachRemaining(f -> finalMergedObject.putIfAbsent(f.getKey(), f.getValue()));
                    } else if (out.isEmpty()) {
                        out.add(sanitizeNode(item));
                    }
                }
                if (mergedObject != null) {
                    out.add(mergedObject);
                }
            }
            return out;
        }
        if (node.isNumber()) return TextNode.valueOf("NUMBER");
        if (node.isBoolean()) return TextNode.valueOf("BOOLEAN");
        return TextNode.valueOf("STRING");
    }

    private static String sanitizeKey(String key) {
        if (key == null || key.length() > 64 || key.matches(".*\\d{5,}.*")) {
            return "<REDACTED_DYNAMIC_KEY>";
        }
        return key;
    }
}