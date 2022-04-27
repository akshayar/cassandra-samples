package org.apache.pulsar.io.cassandra;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.schema.GenericRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonHelper {
	
	/**
     * Convert a JsonNode to an Elasticsearch id.
     */
    public static String stringifyKey(JsonNode jsonNode,ObjectMapper objectMapper) throws JsonProcessingException {
        List<String> fields = new ArrayList<>();
        jsonNode.fieldNames().forEachRemaining(fields::add);
        log.info("stringifyKey fields"+fields);
        return stringifyKey(jsonNode, fields,objectMapper);
    }

    public static String stringifyKey(JsonNode jsonNode, List<String> fields, ObjectMapper objectMapper) throws JsonProcessingException {
        log.info("stringifyKey jsonnode="+jsonNode+",fields="+fields);
        if (fields.size() == 1) {
            JsonNode singleNode = jsonNode.get(fields.get(0));
            String id = objectMapper.writeValueAsString(singleNode);
            return (id.startsWith("\"") && id.endsWith("\""))
                    ? id.substring(1, id.length() - 1)  // remove double quotes
                    : id;
        } else {
            return JsonConverter.toJsonArray(jsonNode, fields).toString();
        }
    }

    public static JsonNode jsonNodeFromFields(JsonNode jsonNode,final List<String> fields) throws JsonProcessingException {
        log.info("jsonNodeFromFields jsonnode="+jsonNode+",fields="+fields);
        if (fields.size() == 1) {
            JsonNode singleNode = jsonNode.get(fields.get(0));
            return singleNode;
        } else {
            return JsonConverter.toJsonArray(jsonNode, fields);
        }
    }

    public static String stringifyValue(Schema<?> schema, Object val,ObjectMapper objectMapper) throws JsonProcessingException {
        JsonNode jsonNode = extractJsonNode(schema, val);
        return objectMapper.writeValueAsString(jsonNode);
    }

    public static JsonNode stripNullNodes(JsonNode node) {
        Iterator<JsonNode> it = node.iterator();
        while (it.hasNext()) {
            JsonNode child = it.next();
            if (child.isNull()) {
                it.remove();
            } else {
                stripNullNodes(child);
            }
        }
        return node;
    }

    public static JsonNode extractJsonNode(Schema<?> schema, Object val) {
        switch (schema.getSchemaInfo().getType()) {
            case JSON:
                return (JsonNode) ((GenericRecord) val).getNativeObject();
            case AVRO:
                org.apache.avro.generic.GenericRecord node = (org.apache.avro.generic.GenericRecord)
                        ((GenericRecord) val).getNativeObject();
                JsonNode node1=JsonConverter.toJson(node);
                log.info("Akshaya Avro extractJsonNode"+node1);
                return node1;
            default:
                throw new UnsupportedOperationException("Unsupported value schemaType="
                        + schema.getSchemaInfo().getType());
        }
    }

}
