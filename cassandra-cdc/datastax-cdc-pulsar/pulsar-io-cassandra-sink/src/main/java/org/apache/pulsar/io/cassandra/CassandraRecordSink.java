package org.apache.pulsar.io.cassandra;

import static org.apache.pulsar.io.cassandra.JsonHelper.extractJsonNode;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.client.api.schema.KeyValueSchema;
import org.apache.pulsar.common.schema.KeyValue;
import org.apache.pulsar.common.schema.SchemaType;
import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.cassandra.dao.AbstractCassandraDao;
import org.apache.pulsar.io.cassandra.dao.CassandraDao;
import org.apache.pulsar.io.core.Sink;
import org.apache.pulsar.io.core.SinkContext;
import org.apache.pulsar.io.core.annotations.Connector;
import org.apache.pulsar.io.core.annotations.IOType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import lombok.extern.slf4j.Slf4j;
/**
 * Cassandra sink that treats incoming messages on the input topic as Strings
 * and write identical key/value pairs.
 */
@Connector(
	    name = "cassandra",
	    type = IOType.SINK,
	    help = "The CassandraStringSink is used for moving messages from Pulsar to Cassandra.",
	    configClass = CassandraSinkConfig.class)
@Slf4j
public class CassandraRecordSink implements Sink<GenericObject>{

	protected CassandraSinkConfig cassandraSinkConfig;
	protected AbstractCassandraDao cassandraDao;

	@Override
	public void close() {
		cassandraDao.close();
	}

	@Override
	public void open(Map<String, Object> config, SinkContext sinkContext) throws Exception {
		cassandraSinkConfig = CassandraSinkConfig.load(config);
		log.info("Initializing with Cassandra Sink Config:" + cassandraSinkConfig);
		cassandraDao = new CassandraDao(cassandraSinkConfig);
	}

	@Override
	public void write(Record<GenericObject> record) throws Exception {
	    Pair<JsonNode, JsonNode> idAndDoc=Pair.of(NullNode.getInstance(), NullNode.getInstance());
	    try {
	        idAndDoc = extractIdAndDocument(record);
	        if (idAndDoc.getRight() == null) {
	            switch (cassandraSinkConfig.getNullValueAction()) {
	                case DELETE:
	                    if (idAndDoc.getLeft() != null) {
	                        if (cassandraSinkConfig.isBulkEnabled()) {
	                            cassandraDao.bulkDelete(record, idAndDoc.getLeft());
	                        } else {
	                            cassandraDao.deleteRecord(record, idAndDoc.getLeft());
	                        }
	                    }
	                    break;
	                case IGNORE:
	                    break;
	                case FAIL:
	                	PulsarClientException.InvalidMessageException e=new PulsarClientException.InvalidMessageException("Unexpected null message value");
	                	log.error("Exception",e);
	                	throw e;
	
	            }
	        } else {
	            if (cassandraSinkConfig.isBulkEnabled()) {
	                cassandraDao.bulkUpsert(record, idAndDoc);
	            } else {
	                cassandraDao.upsertRecord(record, idAndDoc);
	            }
	        }
	    } catch (JsonProcessingException jsonProcessingException) {
	        switch (cassandraSinkConfig.getMalformedDocAction()) {
	            case IGNORE:
	                break;
	            case WARN:
	                warnFailedRecord(record,jsonProcessingException);
	                throw jsonProcessingException;
	            case FAIL:
	                errorFailedRecord(record,jsonProcessingException);
	                throw jsonProcessingException;
	        }
	    } catch (Exception e) {
	        log.error("write error for {} {}:", idAndDoc.getLeft(), idAndDoc.getRight(), e);
	        throw e;
	    }
	}

	private void warnFailedRecord(Record<GenericObject> record, JsonProcessingException jsonProcessingException) {
	    log.warn("Ignoring malformed document messageId={}",
	            record.getMessage().map(Message::getMessageId).orElse(null),
	            jsonProcessingException);
	}

	private void errorFailedRecord(Record<GenericObject> record, JsonProcessingException jsonProcessingException) {
	    log.error("Malformed document messageId={}",
	            record.getMessage().map(Message::getMessageId).orElse(null),
	            jsonProcessingException);
	}

	public CassandraRecordSink() {
		super();
	}

	/**
	 * Extract ES _id and _source using the Schema if available.
	 *
	 * @param record
	 * @return A pair for _id and _source
	 */
	public Pair<JsonNode, JsonNode> extractIdAndDocument(Record<GenericObject> record) throws JsonProcessingException {
	        GenericObject value = null;
	        Schema<?> valueSchema = null;
	        JsonNode id = null;
	        JsonNode doc = null;
	        if (record.getSchema() != null && record.getSchema() instanceof KeyValueSchema) {
	            KeyValueSchema<GenericObject, GenericObject> keyValueSchema = (KeyValueSchema) record.getSchema();
	            Schema<?> keySchema = keyValueSchema.getKeySchema();
	            valueSchema = keyValueSchema.getValueSchema();
	            KeyValue<GenericObject, GenericObject> keyValue =
	                    (KeyValue<GenericObject, GenericObject>) record.getValue().getNativeObject();
	            Object key = keyValue.getKey();
	            value = keyValue.getValue();
	
	            if ( key != null && keySchema != null) {
	                id=extractJsonNode(keySchema, key);
	            }
	
	        } else {
	            valueSchema = record.getSchema();
	            value = record.getValue();
	        }
	
	        if (value != null) {
	
	            if (valueSchema != null) {
	                doc = extractJsonNode(valueSchema, value);
	            } else {
	                String valueStr="";
	                if (value.getNativeObject() instanceof byte[]) {
	                    // for BWC with the ES-Sink
	                    valueStr = new String((byte[]) value.getNativeObject(), StandardCharsets.UTF_8);
	                } else {
	                    valueStr = value.getNativeObject().toString();
	                }
	            }
	
	        }
	
	        log.info("Extracted id:{} , value:{}",id,doc);
	        logDubugIfEnabled(record, id, doc);
	        return Pair.of(id, doc);
	}

	private void logDubugIfEnabled(Record<GenericObject> record, JsonNode id, JsonNode doc) {
	    if (log.isInfoEnabled()) {
	        SchemaType schemaType = null;
	        if (record.getSchema() != null && record.getSchema().getSchemaInfo() != null) {
	            schemaType = record.getSchema().getSchemaInfo().getType();
	        }
	        log.debug("Just Schema recordSchema={}",  record.getSchema().getSchemaInfo().getSchema() );
	        log.debug("Extracted id:{} , value:{}",id,doc);
	    }
	}

}