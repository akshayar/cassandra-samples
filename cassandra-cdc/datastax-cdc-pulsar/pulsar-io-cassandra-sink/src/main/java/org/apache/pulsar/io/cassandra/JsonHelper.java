package org.apache.pulsar.io.cassandra;

import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.schema.GenericRecord;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonHelper {

    public static JsonNode extractJsonNode(Schema<?> schema, Object val) {
        switch (schema.getSchemaInfo().getType()) {
            case JSON:
                return (JsonNode) ((GenericRecord) val).getNativeObject();
            case AVRO:
                org.apache.avro.generic.GenericRecord node = (org.apache.avro.generic.GenericRecord)
                        ((GenericRecord) val).getNativeObject();
                JsonNode node1=JsonConverter.toJson(node);
                log.info("Avro extractJsonNode"+node1);
                return node1;
            default:
                throw new UnsupportedOperationException("Unsupported value schemaType="
                        + schema.getSchemaInfo().getType());
        }
    }

}
