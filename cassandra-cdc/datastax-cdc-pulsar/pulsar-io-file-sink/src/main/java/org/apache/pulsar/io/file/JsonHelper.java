package org.apache.pulsar.io.file;

import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.schema.GenericRecord;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.PrintStream;

@Slf4j
public class JsonHelper {

    public static JsonNode extractJsonNode(Schema<?> schema, Object val, PrintStream json, PrintStream avro) {
        switch (schema.getSchemaInfo().getType()) {
            case JSON:
                return (JsonNode) ((GenericRecord) val).getNativeObject();
            case AVRO:
                org.apache.avro.generic.GenericRecord node = (org.apache.avro.generic.GenericRecord)
                        ((GenericRecord) val).getNativeObject();
                JsonNode node1=JsonConverter.toJson(node);
                json.println(node1);
                avro.println(node);
                log.info("Json Data:"+node1);
                log.info("Avro Data:"+node);
                return node1;
            default:
                throw new UnsupportedOperationException("Unsupported value schemaType="
                        + schema.getSchemaInfo().getType());
        }
    }

}
