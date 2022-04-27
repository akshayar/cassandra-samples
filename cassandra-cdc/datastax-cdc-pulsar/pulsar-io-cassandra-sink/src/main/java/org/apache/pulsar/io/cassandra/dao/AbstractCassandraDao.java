package org.apache.pulsar.io.cassandra.dao;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.functions.api.Record;

import com.fasterxml.jackson.databind.JsonNode;

public interface AbstractCassandraDao {
	
	void close();

	void upsertRecord(Record<GenericObject> record, Pair<JsonNode, JsonNode> idAndDoc);

	void bulkDelete(Record<GenericObject> record, JsonNode left);

	void bulkUpsert(Record<GenericObject> record, Pair<JsonNode, JsonNode> idAndDoc);

	void deleteRecord(Record<GenericObject> record, JsonNode left);

}