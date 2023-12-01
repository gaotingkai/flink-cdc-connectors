/*
 * Copyright 2023 Ververica Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ververica.cdc.composer.flink;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.ververica.cdc.common.annotation.Internal;
import com.ververica.cdc.common.event.Event;
import com.ververica.cdc.common.factories.DataSinkFactory;
import com.ververica.cdc.common.factories.FactoryHelper;
import com.ververica.cdc.common.pipeline.PipelineOptions;
import com.ververica.cdc.common.sink.DataSink;
import com.ververica.cdc.composer.PipelineComposer;
import com.ververica.cdc.composer.PipelineExecution;
import com.ververica.cdc.composer.definition.PipelineDef;
import com.ververica.cdc.composer.definition.SinkDef;
import com.ververica.cdc.composer.flink.coordination.OperatorIDGenerator;
import com.ververica.cdc.composer.flink.translator.DataSinkTranslator;
import com.ververica.cdc.composer.flink.translator.DataSourceTranslator;
import com.ververica.cdc.composer.flink.translator.PartitioningTranslator;
import com.ververica.cdc.composer.flink.translator.SchemaOperatorTranslator;
import com.ververica.cdc.composer.utils.FactoryDiscoveryUtils;

import java.nio.file.Path;
import java.util.List;

/** Composer for translating data pipeline to a Flink DataStream job. */
@Internal
public class FlinkPipelineComposer implements PipelineComposer {

    private final StreamExecutionEnvironment env;
    private final boolean isBlocking;

    public static FlinkPipelineComposer ofRemoteCluster(
            String host, int port, List<Path> additionalJars) {
        String[] jarPaths = additionalJars.stream().map(Path::toString).toArray(String[]::new);
        return new FlinkPipelineComposer(
                StreamExecutionEnvironment.createRemoteEnvironment(host, port, jarPaths), false);
    }

    public static FlinkPipelineComposer ofMiniCluster() {
        return new FlinkPipelineComposer(StreamExecutionEnvironment.createLocalEnvironment(), true);
    }

    private FlinkPipelineComposer(StreamExecutionEnvironment env, boolean isBlocking) {
        this.env = env;
        this.isBlocking = isBlocking;
    }

    @Override
    public PipelineExecution compose(PipelineDef pipelineDef) {
        int parallelism = pipelineDef.getConfig().get(PipelineOptions.GLOBAL_PARALLELISM);
        env.getConfig().setParallelism(parallelism);

        // Source
        DataSourceTranslator sourceTranslator = new DataSourceTranslator();
        DataStream<Event> stream =
                sourceTranslator.translate(pipelineDef.getSource(), env, parallelism);

        // Create sink in advance as schema operator requires MetadataApplier
        DataSink dataSink = createDataSink(pipelineDef.getSink());

        // Schema operator
        SchemaOperatorTranslator schemaOperatorTranslator =
                new SchemaOperatorTranslator(
                        pipelineDef.getConfig().get(PipelineOptions.SCHEMA_CHANGE_BEHAVIOR),
                        pipelineDef.getConfig().get(PipelineOptions.SCHEMA_OPERATOR_UID));
        stream =
                schemaOperatorTranslator.translate(
                        stream, parallelism, dataSink.getMetadataApplier());
        OperatorIDGenerator schemaOperatorIDGenerator =
                new OperatorIDGenerator(schemaOperatorTranslator.getSchemaOperatorUid());

        // Add partitioner
        PartitioningTranslator partitioningTranslator = new PartitioningTranslator();
        stream =
                partitioningTranslator.translate(
                        stream, parallelism, parallelism, schemaOperatorIDGenerator.generate());

        // Sink
        DataSinkTranslator sinkTranslator = new DataSinkTranslator();
        sinkTranslator.translate(stream, dataSink, schemaOperatorIDGenerator.generate());

        return new FlinkPipelineExecution(
                env, pipelineDef.getConfig().get(PipelineOptions.PIPELINE_NAME), isBlocking);
    }

    private DataSink createDataSink(SinkDef sinkDef) {
        // Search the data sink factory
        DataSinkFactory sinkFactory =
                FactoryDiscoveryUtils.getFactoryByIdentifier(
                        sinkDef.getType(), DataSinkFactory.class);

        // Include sink connector JAR
        FactoryDiscoveryUtils.getJarPathByIdentifier(sinkDef.getType(), DataSinkFactory.class)
                .ifPresent(jar -> FlinkEnvironmentUtils.addJar(env, jar));

        // Create data sink
        return sinkFactory.createDataSink(
                new FactoryHelper.DefaultContext(
                        sinkDef.getConfig().toMap(),
                        sinkDef.getConfig(),
                        Thread.currentThread().getContextClassLoader()));
    }
}
