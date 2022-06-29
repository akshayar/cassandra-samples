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

package org.apache.pulsar.io.file;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.pulsar.io.core.annotations.FieldDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ConsoleSinkConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @FieldDoc(
        required = true,
        defaultValue = "",
        help = "Json File Path to log data to")
    private String fileJson;
    
    @FieldDoc(
        required = true,
        defaultValue = "",
        help = "File to record Avro Data")
    private String fileAvro;
    


    public static ConsoleSinkConfig load(String yamlFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(yamlFile), ConsoleSinkConfig.class);
    }

    public static ConsoleSinkConfig load(Map<String, Object> map) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new ObjectMapper().writeValueAsString(map), ConsoleSinkConfig.class);
    }
}