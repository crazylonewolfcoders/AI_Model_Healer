package io.github.crazy.lonewolf.coders.omnij.plugin.parser.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.crazy.lonewolf.coders.omnij.plugin.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.config.OmniJConfig;
import io.github.crazy.lonewolf.coders.omnij.plugin.exceptions.JsonTemplateProcessingException;
import io.github.crazy.lonewolf.coders.omnij.plugin.parser.ITemplateEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;


public class JsonTemplateEngine implements ITemplateEngine {

@Autowired
private OmniJConfig config;

    @Override
        public Object processTemplateAndGenerateOutPut(Template template, String inboundJson) throws JsonTemplateProcessingException {
           try {
                ObjectMapper mapper = new ObjectMapper();

               JsonNode templateTree = mapper.readTree(template.template());
               JsonNode inboundTree = mapper.readTree(inboundJson);

               resolvePlaceholders(mapper,templateTree, inboundTree, null);
               if(config.getJsonvalidations().isEnableMandatoryChecks()){
               //    mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

               }
                Class<?> clazz = Class.forName(template.clazzName());
               return mapper.treeToValue(templateTree,clazz);
           }catch (Exception e){
               throw new JsonTemplateProcessingException(template,e);
           }
        }


        private  void resolvePlaceholders(  ObjectMapper mapper,JsonNode currentTemplateNode, JsonNode inboundTree, JsonNode currentLoopItem) {
            if (currentTemplateNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) currentTemplateNode;

                // Check if this object represents a dynamic Array Loop mapping
                if (objectNode.has("[]from") && objectNode.has("[]template")) {
                    handleArrayLoop(mapper,objectNode, inboundTree, currentLoopItem);
                    return;
                }

                Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    JsonNode valueNode = field.getValue();

                    if (valueNode.isTextual() ) {
                        JsonNode actualValue = extractValue(mapper,valueNode.asText(), inboundTree, currentLoopItem);
                        objectNode.set(field.getKey(), actualValue);
                    } else {
                        resolvePlaceholders(mapper,valueNode, inboundTree, currentLoopItem);
                    }
                }
            } else if (currentTemplateNode.isArray()) {
                for (JsonNode element : currentTemplateNode) {
                    resolvePlaceholders(mapper,element, inboundTree, currentLoopItem);
                }
            }
        }

        private  void handleArrayLoop(ObjectMapper mapper, ObjectNode arrayMappingNode, JsonNode inboundTree, JsonNode currentLoopItem) {
            String fromPath = arrayMappingNode.get("[]from").asText();
            JsonNode templateStructure = arrayMappingNode.get("[]template");

            // Extract the target array from the source data
            JsonNode sourceArray = extractValue(mapper,fromPath, inboundTree, currentLoopItem);
            ArrayNode targetArray = mapper.createArrayNode();

            if (sourceArray.isArray()) {
                for (JsonNode item : sourceArray) {
                    // Deep copy the template structure for this specific item iteration
                    JsonNode itemInstance = templateStructure.deepCopy();

                    // Process the template instance, passing the single item context
                    resolvePlaceholders(mapper,itemInstance, inboundTree, item);
                    targetArray.add(itemInstance);
                }
            }

            // Replace the special structural object node with the actual computed array node
            arrayMappingNode.removeAll();
            // Since we cannot change the parent structure easily, we shift array elements up later,
            // or cleaner: we change the parent object iteration to replace the property.
            // To make it simple without altering the iterator, we utilize an internal wrapper trick:
            arrayMappingNode.set("temp_array_marker", targetArray);
        }

        private static JsonNode extractValue(ObjectMapper mapper,String expression, JsonNode inboundTree, JsonNode currentLoopItem) {

            // If we are currently executing inside a loop context, check the local item first
            if (currentLoopItem != null) {

                JsonNode itemValue = currentLoopItem.at(expression);
                if (!itemValue.isMissingNode()) {
                    return itemValue;
                }
            }

            // Fallback or default to checking the root inbound structure
            JsonNode rootValue = inboundTree.at(expression);
            return !rootValue.isMissingNode() ? rootValue : mapper.nullNode();
        }

}
