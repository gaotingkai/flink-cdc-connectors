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

package com.ververica.cdc.composer.utils.factory;

import com.ververica.cdc.common.configuration.ConfigOption;
import com.ververica.cdc.common.factories.DataSourceFactory;
import com.ververica.cdc.common.source.DataSource;
import com.ververica.cdc.common.source.EventSourceProvider;
import com.ververica.cdc.common.source.MetadataAccessor;

import java.util.HashSet;
import java.util.Set;

/** A dummy {@link DataSourceFactory} for testing. */
public class DataSourceFactory1 implements DataSourceFactory {
    @Override
    public DataSource createDataSource(Context context) {
        return new TestDataSource(context.getConfiguration().get(TestOptions.HOST));
    }

    @Override
    public String identifier() {
        return "data-source-factory-1";
    }

    @Override
    public Set<ConfigOption<?>> requiredOptions() {
        return new HashSet<>();
    }

    @Override
    public Set<ConfigOption<?>> optionalOptions() {
        Set<ConfigOption<?>> options = new HashSet<>();
        options.add(TestOptions.HOST);
        return options;
    }

    /** A dummy {@link DataSource} for testing. */
    public static class TestDataSource implements DataSource {

        private final String host;

        public TestDataSource(String host) {
            this.host = host;
        }

        public String getHost() {
            return host;
        }

        @Override
        public EventSourceProvider getEventSourceProvider() {
            return null;
        }

        @Override
        public MetadataAccessor getMetadataAccessor() {
            return null;
        }
    }
}
