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

package com.ververica.cdc.connectors.base.source.assigner.splitter;

import com.ververica.cdc.common.annotation.Experimental;
import com.ververica.cdc.connectors.base.source.meta.split.SnapshotSplit;
import io.debezium.relational.TableId;

import java.util.Collection;

/** The splitter used to split collection into a set of chunks. */
@Experimental
public interface ChunkSplitter {

    /** Generates all snapshot splits (chunks) for the give data collection. */
    Collection<SnapshotSplit> generateSplits(TableId tableId);
}
