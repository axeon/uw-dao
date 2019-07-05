package uw.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.dao.conf.DaoConfigManager;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * sequence管理器。 如果不是使用sequence管理器，在集群环境中将有一些麻烦。 系统会自动判定启用那种方式的id生成方式.
 *
 * @author zhangjin
 */
public class SequenceManager {

    /**
     * 日志.
     */
    private static final Logger logger = LoggerFactory.getLogger(SequenceManager.class);
    /**
     * 初始化seq.
     */
    private static final String INIT_SEQ = "insert into sys_seq (seq_name,seq_id,seq_desc,increment_num,create_date,last_update) values(?,?,?,?,?,?)";
    /**
     * 载入当前seq.
     */
    private static final String LOAD_SEQ = "select seq_id,increment_num from sys_seq where seq_name=? ";

    /**
     * 确认更新seq.
     */
    private static final String UPDATE_SEQ = "update sys_seq set seq_id=?,last_update=? where seq_name=? and seq_id=?";

    /**
     * 重置seq.
     */
    private static final String RESET_SEQ = "update sys_seq set seq_id=?,increment_num=?,last_update=? where seq_name=?";

    /**
     * SequenceManager集合.
     */
    private static final Map<String, SequenceManager> seqManager = new ConcurrentHashMap<String, SequenceManager>();

    /**
     * dao实例。
     */
    private static final DaoFactory dao = DaoFactory.getInstance();

    /**
     * 重试次数。
     */
    private static final int MAX_RETRY_TIMES = 100;
    /**
     * 当前id.
     */
    private final AtomicLong currentId = new AtomicLong(0);

    /**
     * sequenceName.
     */
    private final String sequenceName;

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
    public SequenceManager(String seqName) {
        this.sequenceName = seqName;
    }

    /**
     * 返回指定的表的sequenceId数值.
     *
     * @param seqName 表名
     * @return 下一个值
     */
    public static long nextId(String seqName) {
        SequenceManager manager = seqManager.computeIfAbsent(seqName, x -> new SequenceManager(seqName));
        return manager.nextId(1);
    }


    /**
     * 申请一个Id号码范围.
     *
     * @param seqName 表名
     * @param range   申请多少个号码
     * @return 起始号码
     */
    public static long allocateIdRange(String seqName, int range) {
        SequenceManager manager = seqManager.computeIfAbsent(seqName, x -> new SequenceManager(seqName));
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
        SequenceManager manager = seqManager.computeIfAbsent(sequenceName, x -> new SequenceManager(sequenceName));
        return manager.resetSeq(initSeq, incrementNum);
    }

    /**
     * 返回下一个可以取到的id值,功能上类似于数据库的自动递增字段.
     *
     * @param value 递增累加值
     */
    private long nextId(int value) {
        if (currentId.get() + value > maxId) {
            getNextBlock(value);
        }
        return currentId.getAndAdd(value);
    }

    /**
     * 通过多次尝试获得下一组sequenceId.
     *
     * @param value 递增累加值
     */
    private void getNextBlock(int value) {
        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            if (getNextBlockImpl(value)) {
                break;
            }
            logger.warn("WARNING: SequenceManager failed to obtain Sequence[{}] next ID block . Trying {}...", this.sequenceName, i + 1);
            // 如果不成功，再次调用改方法。
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
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
    private synchronized boolean getNextBlockImpl(int value) {
        // 从数据库中获取当前值。
        loadSeq();
        // 自动递增id到我们规定的递增累加值。
        long newID = currentId.get() + Math.max(incrementNum, value);
        boolean success = false;
        try {
            int effect = dao.executeCommand(DaoConfigManager.getRouteMapping("sys_sequence", "all"), UPDATE_SEQ, new Object[]{newID, new Date(), sequenceName, currentId.get()});
            success = (effect == 1);
            if (success) {
                this.maxId = newID;
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
            int effect = dao.executeCommand(DaoConfigManager.getRouteMapping("sys_sequence", "all"), RESET_SEQ, new Object[]{initSeq, incrementNum, new Date(), sequenceName});
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
     * 载入序列.
     */
    private void loadSeq() {
        try {
            DataSet ds = dao.queryForDataSet(DaoConfigManager.getRouteMapping("sys_sequence", "all"), LOAD_SEQ, new Object[]{sequenceName});
            if (ds.next()) {
                currentId.set(ds.getLong(1));
                incrementNum = ds.getInt(2);
            } else {
                initSeq();
            }
        } catch (TransactionException e) {
            logger.error("loadSeq Error!", e);
        }
    }

    /**
     * 初始化序列.
     */
    private void initSeq() {
        try {
            dao.executeCommand(DaoConfigManager.getRouteMapping("sys_sequence", "all"), INIT_SEQ, new Object[]{sequenceName, currentId.get(), sequenceName, incrementNum, new Date(), new Date()});
        } catch (TransactionException e) {
            logger.error("initSeq exception!", e);
        }
    }

}
