package uw.dao;

import org.openjdk.jmh.runner.RunnerException;
import uw.dao.conf.DaoConfig;
import uw.dao.conf.DaoConfigManager;

import java.util.HashMap;

public class SeqTest {

    public static void main(String[] args) throws RunnerException {
        setup();
//        if (SequenceFactory.nextId(seqName)==1){
//            SequenceFactory.resetSeq(seqName,1234567,100);
//        }
        String seqName = "TestRandom3";
        System.out.println(SequenceFactory.nextId(seqName));
//        System.out.println(SequenceFactory.nextId(seqName));
//        System.out.println(SequenceFactory.nextId(seqName));
//        System.out.println(SequenceFactory.nextId(seqName));
//        System.out.println(SequenceFactory.nextId(seqName));
//        System.out.println(SequenceFactory.nextId(seqName));
//        System.out.println(SequenceFactory.nextId(seqName));
//        System.out.println(SequenceFactory.nextId(seqName));
//        System.out.println(SequenceFactory.nextId(seqName));
//        System.out.println(SequenceFactory.nextId(seqName));
//        System.out.println(SequenceFactory.nextId(seqName));
//        System.out.println(SequenceFactory.nextId(seqName));
//        System.out.println(SequenceFactory.allocateIdRange(seqName, 100));
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
