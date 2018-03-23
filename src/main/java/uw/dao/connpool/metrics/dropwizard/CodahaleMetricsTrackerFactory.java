/*
 * Copyright (C) 2013 Brett Wooldridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uw.dao.connpool.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import uw.dao.connpool.metrics.IMetricsTracker;
import uw.dao.connpool.metrics.MetricsTrackerFactory;
import uw.dao.connpool.metrics.PoolStats;


public final class CodahaleMetricsTrackerFactory implements MetricsTrackerFactory {
    private final MetricRegistry registry;

    public CodahaleMetricsTrackerFactory(MetricRegistry registry) {
        this.registry = registry;
    }

    public MetricRegistry getRegistry() {
        return registry;
    }

    @Override
    public IMetricsTracker create(String poolName, PoolStats poolStats) {
        return new CodaHaleMetricsTracker(poolName, poolStats, registry);
    }
}
