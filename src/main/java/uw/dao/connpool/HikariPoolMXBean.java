package uw.dao.connpool;

/**
 * The javax.management MBean for a Hikari pool instance.
 *
 * @author Brett Wooldridge
 */
public interface HikariPoolMXBean {

    /**
     * 当前空闲连接数。
     *
     * @return
     */
    int getIdleConnections();

    /**
     * 当前使用中连接数。
     * @return
     */
    int getActiveConnections();

    /**
     * 总连接数。
     * @return
     */
    int getTotalConnections();

    /**
     * 等待线程数。
     * @return
     */
    int getThreadsAwaitingConnection();

    /**
     * softEvict连接数。
     */
    void softEvictConnections();

    /**
     * 暂停连接池。
     */
    void suspendPool();

    /**
     * 继续连接池.
     */
    void resumePool();
}
