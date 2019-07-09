package uw.dao;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import uw.dao.conf.DaoConfig;
import uw.dao.conf.DaoConfigManager;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})//基准测试类型
@OutputTimeUnit(TimeUnit.SECONDS)//基准测试结果的时间类型
@Warmup(iterations = 3)//预热的迭代次数
@Threads(2)//测试线程数量
@State(Scope.Benchmark)//该状态为每个线程独享
//度量:iterations进行测试的轮次，time每轮进行的时长，timeUnit时长单位,batchSize批次数量
@Measurement(iterations = 10, time = -1, timeUnit = TimeUnit.SECONDS, batchSize = -1)
public class SqlBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SqlBenchmark.class.getSimpleName())
                .forks(0)
                .build();
        new Runner(opt).run();
    }

    static DaoFactory dao = DaoFactory.getInstance();

    @Setup
    public static void setup() {
        DaoConfig daoConfig = new DaoConfig();
        DaoConfig.ConnPool pool = new DaoConfig.ConnPool();
        DaoConfig.ConnPoolConfig poolConfig = new DaoConfig.ConnPoolConfig();
        poolConfig.setDriver("com.mysql.jdbc.Driver");
        poolConfig.setUrl("jdbc:mysql://192.168.88.88:3300/saas?characterEncoding=utf-8&useSSL=false&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true");
        poolConfig.setUsername("root");
        poolConfig.setPassword("WR5QmENQQCHGCNps");
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

    private static final String LOAD_SEQ = "select seq_id,increment_num from sys_seq where seq_name=? ";

    private static final String UPDATE_SEQ = "update sys_seq set seq_id=?,last_update=? where seq_name=? and seq_id=?";

    @Benchmark
    @CompilerControl(CompilerControl.Mode.INLINE)
    public static synchronized  void getSeq() throws SQLException, TransactionException {

        DataSet ds = dao.queryForDataSet(DaoConfigManager.getRouteMapping("sys_seq", "all"), LOAD_SEQ, new Object[]{"test"});
        if (ds.next()) {
            ds.getLong(1);
            ds.getInt(2);
        }
        int effect = dao.executeCommand(DaoConfigManager.getRouteMapping("sys_seq", "all"), UPDATE_SEQ, new Object[]{100, new Date(), "test", 100});

    }

}
