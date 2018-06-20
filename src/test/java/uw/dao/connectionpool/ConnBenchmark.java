package uw.dao.connectionpool;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import uw.dao.conf.DaoConfig;
import uw.dao.conf.DaoConfigManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput,Mode.AverageTime})//基准测试类型
@OutputTimeUnit(TimeUnit.SECONDS)//基准测试结果的时间类型
@Warmup(iterations = 3)//预热的迭代次数
@Threads(100)//测试线程数量
@State(Scope.Benchmark)//该状态为每个线程独享
//度量:iterations进行测试的轮次，time每轮进行的时长，timeUnit时长单位,batchSize批次数量
@Measurement(iterations = 10, time = -1, timeUnit = TimeUnit.SECONDS, batchSize = -1)
public class ConnBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ConnBenchmark.class.getSimpleName())
                .forks(0)
                .build();
        new Runner(opt).run();
    }


    @Setup
    public static void setup() {
        DaoConfig daoConfig = new DaoConfig();
        DaoConfig.ConnPool pool = new DaoConfig.ConnPool();
        DaoConfig.ConnPoolConfig poolConfig = new DaoConfig.ConnPoolConfig();
        poolConfig.setDriver("uw.dao.connectionpool.stubs.StubDriver");
        poolConfig.setUrl("abc");
        poolConfig.setUsername("test");
        poolConfig.setPassword("test");
        poolConfig.setMinConn(3);
        poolConfig.setMaxConn(100);
        poolConfig.setConnMaxAge(3600);
        poolConfig.setConnBusyTimeout(120);
        poolConfig.setConnIdleTimeout(120);
        pool.setRoot(poolConfig);
        pool.setList(new HashMap<>());
        daoConfig.setConnPool(pool);
        DaoConfigManager.setConfig(daoConfig);
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.INLINE)
    public static Connection cycleCnnection() throws SQLException {
        Connection connection = ConnectionManager.getConnection();
        connection.close();
        return connection;
    }


}
