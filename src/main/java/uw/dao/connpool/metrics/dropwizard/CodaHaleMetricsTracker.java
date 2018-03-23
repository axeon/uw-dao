/*
 * Copyright (C) 2013,2014 Brett Wooldridge
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

import com.codahale.metrics.*;
import uw.dao.connpool.metrics.IMetricsTracker;
import uw.dao.connpool.metrics.PoolStats;

import java.util.concurrent.TimeUnit;

public final class CodaHaleMetricsTracker implements IMetricsTracker {
    private static final String METRIC_CATEGORY = "pool";
    private static final String METRIC_NAME_WAIT = "Wait";
    private static final String METRIC_NAME_USAGE = "Usage";
    private static final String METRIC_NAME_CONNECT = "ConnectionCreation";
    private static final String METRIC_NAME_TIMEOUT_RATE = "ConnectionTimeoutRate";
    private static final String METRIC_NAME_TOTAL_CONNECTIONS = "TotalConnections";
    private static final String METRIC_NAME_IDLE_CONNECTIONS = "IdleConnections";
    private static final String METRIC_NAME_ACTIVE_CONNECTIONS = "ActiveConnections";
    private static final String METRIC_NAME_PENDING_CONNECTIONS = "PendingConnections";
    private static final String METRIC_NAME_MAX_CONNECTIONS = "MaxConnections";
    private static final String METRIC_NAME_MIN_CONNECTIONS = "MinConnections";
    private final String poolName;
    private final Timer connectionObtainTimer;
    private final Histogram connectionUsage;
    private final Histogram connectionCreation;
    private final Meter connectionTimeoutMeter;
    private final MetricRegistry registry;

    public CodaHaleMetricsTracker(final String poolName, final PoolStats poolStats, final MetricRegistry registry) {
        this.poolName = poolName;
        this.registry = registry;
        this.connectionObtainTimer = registry.timer(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_WAIT));
        this.connectionUsage = registry.histogram(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_USAGE));
        this.connectionCreation = registry.histogram(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_CONNECT));
        this.connectionTimeoutMeter = registry.meter(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_TIMEOUT_RATE));

        registry.register(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_TOTAL_CONNECTIONS),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return poolStats.getTotalConnections();
                    }
                });

        registry.register(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_IDLE_CONNECTIONS),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return poolStats.getIdleConnections();
                    }
                });

        registry.register(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_ACTIVE_CONNECTIONS),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return poolStats.getActiveConnections();
                    }
                });

        registry.register(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_PENDING_CONNECTIONS),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return poolStats.getPendingThreads();
                    }
                });

        registry.register(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_MAX_CONNECTIONS),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return poolStats.getMaxConnections();
                    }
                });

        registry.register(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_MIN_CONNECTIONS),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return poolStats.getMinConnections();
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        registry.remove(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_WAIT));
        registry.remove(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_USAGE));
        registry.remove(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_CONNECT));
        registry.remove(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_TIMEOUT_RATE));
        registry.remove(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_TOTAL_CONNECTIONS));
        registry.remove(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_IDLE_CONNECTIONS));
        registry.remove(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_ACTIVE_CONNECTIONS));
        registry.remove(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_PENDING_CONNECTIONS));
        registry.remove(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_MAX_CONNECTIONS));
        registry.remove(MetricRegistry.name(poolName, METRIC_CATEGORY, METRIC_NAME_MIN_CONNECTIONS));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordConnectionAcquiredNanos(final long elapsedAcquiredNanos) {
        connectionObtainTimer.update(elapsedAcquiredNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordConnectionUsageMillis(final long elapsedBorrowedMillis) {
        connectionUsage.update(elapsedBorrowedMillis);
    }

    @Override
    public void recordConnectionTimeout() {
        connectionTimeoutMeter.mark();
    }

    @Override
    public void recordConnectionCreatedMillis(long connectionCreatedMillis) {
        connectionCreation.update(connectionCreatedMillis);
    }

    public Timer getConnectionAcquisitionTimer() {
        return connectionObtainTimer;
    }

    public Histogram getConnectionDurationHistogram() {
        return connectionUsage;
    }

    public Histogram getConnectionCreationHistogram() {
        return connectionCreation;
    }
}
