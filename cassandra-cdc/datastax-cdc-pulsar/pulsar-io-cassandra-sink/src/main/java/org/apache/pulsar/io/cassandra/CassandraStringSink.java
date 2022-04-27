/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pulsar.io.cassandra;

import java.util.Map;

import org.apache.pulsar.io.cassandra.dao.CassandraDao;
import org.apache.pulsar.io.core.SinkContext;
import org.apache.pulsar.io.core.annotations.Connector;
import org.apache.pulsar.io.core.annotations.IOType;

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
public class CassandraStringSink extends CassandraAbstractSink  {
    @Override
    public void open(Map<String, Object> config, SinkContext sinkContext) throws Exception {
        cassandraSinkConfig=CassandraSinkConfig.load(config);
        log.info("Initializing with Cassandra Sink Config:"+cassandraSinkConfig);
        cassandraDao=new CassandraDao(cassandraSinkConfig);
    }

}