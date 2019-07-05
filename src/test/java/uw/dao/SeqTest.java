package uw.dao;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import uw.dao.conf.DaoConfig;
import uw.dao.conf.DaoConfigManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SeqTest {

    public static void main(String[] args) throws RunnerException {
        setup();
//        if (SequenceManager.nextId("TestRandom")==1){
//            SequenceManager.resetSeq("TestRandom",1234567,100);
//        }
        System.out.println(SequenceManager.nextId("TestRandom"));
        System.out.println(SequenceManager.nextId("TestRandom"));
        System.out.println(SequenceManager.nextId("TestRandom"));
        System.out.println(SequenceManager.nextId("TestRandom"));
        System.out.println(SequenceManager.nextId("TestRandom"));
        System.out.println(SequenceManager.nextId("TestRandom"));
        System.out.println(SequenceManager.nextId("TestRandom"));
        System.out.println(SequenceManager.nextId("TestRandom"));
        System.out.println(SequenceManager.nextId("TestRandom"));
        System.out.println(SequenceManager.nextId("TestRandom"));
        System.out.println(SequenceManager.nextId("TestRandom"));
        System.out.println(SequenceManager.nextId("TestRandom"));
//        System.out.println(SequenceManager.allocateIdRange("TestRandom", 100));
    }


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
}
