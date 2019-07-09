package uw.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.dao.conf.DaoConfigManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sequence工厂类，可以在集群环境中可靠使用。
 * incrementNum是非常重要的参数，当其为1(默认值)时，单线程tps≈(30~50)，多线程略高。
 * incrementNum和tps的关系公式为 tps = incrementNum*(30~50)。
 *
 * @author zhangjin
 */
public class SequenceFactory {

    /**
     * 日志.
     */
    private static final Logger logger = LoggerFactory.getLogger(SequenceFactory.class);
    /**
     * 初始化seq.
     */
    private static final String INIT_SEQ = "insert into sys_seq (seq_name,seq_id,seq_desc,increment_num,create_date,last_update) values(?,?,?,?,now(),now())";
    /**
     * 载入当前seq.
     */
    private static final String LOAD_SEQ = "select seq_id,increment_num from sys_seq where seq_name=? ";

    /**
     * 确认更新seq.
     */
    private static final String UPDATE_SEQ = "update sys_seq set seq_id=?,last_update=now() where seq_name=? and seq_id=?";

    /**
     * 重置seq.
     */
    private static final String RESET_SEQ = "update sys_seq set seq_id=?,increment_num=?,last_update=now() where seq_name=?";

    /**
     * SequenceManager集合.
     */
    private static final Map<String, SequenceFactory> seqFactory = new ConcurrentHashMap<String, SequenceFactory>();

    /**
     * dao实例。
     */
    private static final DaoFactory dao = DaoFactory.getInstance();

    /**
     * 重试次数。
     */
    private static final int MAX_RETRY_TIMES = 100;


    /**
     * seqName.
     */
    private final String seqName;

    /**
     * 当前id.
     */
    private long currentId = 0;

    /**
     * 当前可以获取的最大id.
     */
    private long maxId = 0;

    /**
     * 增量数，高并发应用应该保持较高的增量数字。
     */
    private int incrementNum = 1;

    /**
     * 建立一个Sequence实例.
     *
     * @param seqName seq名称。
     */
    public SequenceFactory(String seqName) {
        this.seqName = seqName;
    }

    /**
     * 返回指定的表的sequenceId数值.
     *
     * @param seqName 表名
     * @return 下一个值
     */
    public static long nextId(String seqName) {
        SequenceFactory manager = seqFactory.computeIfAbsent(seqName, x -> new SequenceFactory(seqName));
        return manager.nextId(1);
    }


    /**
     * 申请一个Id号码范围，范围从当前id到id+range。
     *
     * @param seqName 表名
     * @param range   申请多少个号码
     * @return 起始号码
     */
    public static long allocateIdRange(String seqName, int range) {
        SequenceFactory manager = seqFactory.computeIfAbsent(seqName, x -> new SequenceFactory(seqName));
        return manager.nextId(range);
    }

    /**
     * 重置sequence信息。
     *
     * @param sequenceName sequence名字
     * @param initSeq      初始值。
     * @param incrementNum 递增数。
     * @return
     */
    public static boolean resetSeq(String sequenceName, long initSeq, int incrementNum) {
        SequenceFactory manager = seqFactory.computeIfAbsent(sequenceName, x -> new SequenceFactory(sequenceName));
        return manager.resetSeq(initSeq, incrementNum);
    }

    /**
     * 返回下一个可以取到的id值,功能上类似于数据库的自动递增字段。
     * 由于集群环境下的资源竞争，系统最多会重试10s。
     * 如果返回-1，则说明获取失败。
     *
     * @param value seq值，如果=-1，则说明有问题。
     */
    private synchronized long nextId(int value) {
        if (currentId + value > maxId) {
            if (!getNextBlock(value)) {
                return -1;
            }
        }
        return ++currentId;
    }

    /**
     * 通过多次尝试获得下一组sequenceId.
     *
     * @param value 递增累加值
     */
    private boolean getNextBlock(int value) {
        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            if (getNextBlockImpl(value)) {
                return true;
            }
            logger.warn("WARNING: SequenceFactory failed to obtain Sequence[{}] next ID block . Trying {}...", this.seqName, i + 1);
            // 如果不成功，再次调用改方法。
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 执行一个查找下一个sequenceId的操作。步骤如下：
     * <ol>
     * <li>从当前的数据库中select出当前的Id
     * <li>自动递增id。
     * <li>Update db row with new id where id=old_id.
     * <li>如果update失败，会重复执行，直至成功。
     * </ol>
     *
     * @param value 递增累加值
     * @return boolean
     */
    private boolean getNextBlockImpl(int value) {
        boolean success = false;
        try {
            String connName = DaoConfigManager.getRouteMapping("sys_seq", "all");
            // 从数据库中获取当前值。
            DataSet ds = dao.queryForDataSet(connName, LOAD_SEQ, new Object[]{seqName});
            if (ds.next()) {
                currentId = ds.getLong(1);
                incrementNum = ds.getInt(2);
            } else {
                initSeq();
            }
            // 自动递增id到我们规定的递增累加值。
            long nid = currentId + Math.max(incrementNum, value);
            int effect = dao.executeCommand(connName, UPDATE_SEQ, new Object[]{nid, seqName, currentId});
            success = (effect == 1);
            if (success) {
                this.maxId = nid;
            }
        } catch (TransactionException e) {
            logger.error("GetNextBlock Error!", e);
        }
        return success;
    }

    /**
     * 重置sequence信息。
     *
     * @param initSeq      初始值。
     * @param incrementNum 递增数。
     * @return
     */
    private boolean resetSeq(long initSeq, int incrementNum) {
        boolean success = false;
        try {
            int effect = dao.executeCommand(DaoConfigManager.getRouteMapping("sys_seq", "all"), RESET_SEQ, new Object[]{initSeq, incrementNum, seqName});
            success = (effect == 1);
            if (success) {
                this.maxId = 0;
            }
        } catch (TransactionException e) {
            logger.error("GetNextBlock Error!", e);
        }
        return success;
    }

    /**
     * 初始化序列.
     */
    private void initSeq() {
        try {
            dao.executeCommand(DaoConfigManager.getRouteMapping("sys_seq", "all"), INIT_SEQ, new Object[]{seqName, currentId, seqName, incrementNum});
        } catch (TransactionException e) {
            logger.error("initSeq exception!", e);
        }
    }

}
