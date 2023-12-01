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

package com.ververica.cdc.common.pipeline;

import com.ververica.cdc.common.annotation.PublicEvolving;
import com.ververica.cdc.common.configuration.ConfigOption;
import com.ververica.cdc.common.configuration.ConfigOptions;
import com.ververica.cdc.common.configuration.description.Description;
import com.ververica.cdc.common.configuration.description.ListElement;

import static com.ververica.cdc.common.configuration.description.TextElement.text;

/** Predefined pipeline configuration options. */
@PublicEvolving
public class PipelineOptions {

    public static final ConfigOption<String> PIPELINE_NAME =
            ConfigOptions.key("pipeline.name")
                    .stringType()
                    .defaultValue("Flink CDC Pipeline Job")
                    .withDescription("The name of the pipeline");

    public static final ConfigOption<Integer> GLOBAL_PARALLELISM =
            ConfigOptions.key("pipeline.global.parallelism")
                    .intType()
                    .noDefaultValue()
                    .withDescription("The global parallelism of the pipeline");

    public static final ConfigOption<SchemaChangeBehavior> SCHEMA_CHANGE_BEHAVIOR =
            ConfigOptions.key("pipeline.schema.change.behavior")
                    .enumType(SchemaChangeBehavior.class)
                    .defaultValue(SchemaChangeBehavior.EVOLVE)
                    .withDescription(
                            Description.builder()
                                    .text("Behavior for handling schema change events. ")
                                    .linebreak()
                                    .add(
                                            ListElement.list(
                                                    text(
                                                            "EVOLVE: Apply schema changes to downstream. This requires sink to support handling schema changes."),
                                                    text("IGNORE: Drop all schema change events."),
                                                    text(
                                                            "EXCEPTION: Throw an exception to terminate the sync pipeline.")))
                                    .build());

    public static final ConfigOption<String> SCHEMA_OPERATOR_UID =
            ConfigOptions.key("pipeline.schema.operator.uid")
                    .stringType()
                    .defaultValue("$$_schema_operator_$$")
                    .withDescription(
                            "The unique ID for schema operator. This ID will be used for inter-operator communications and must be unique across operators.");

    private PipelineOptions() {}
}
