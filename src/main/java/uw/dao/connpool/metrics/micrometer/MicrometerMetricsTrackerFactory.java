package uw.dao.connpool.metrics.micrometer;


import io.micrometer.core.instrument.MeterRegistry;
import uw.dao.connpool.metrics.IMetricsTracker;
import uw.dao.connpool.metrics.MetricsTrackerFactory;
import uw.dao.connpool.metrics.PoolStats;

public class MicrometerMetricsTrackerFactory implements MetricsTrackerFactory {

    private final MeterRegistry registry;

    public MicrometerMetricsTrackerFactory(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public IMetricsTracker create(String poolName, PoolStats poolStats) {
        return new MicrometerMetricsTracker(poolName, poolStats, registry);
    }
}
