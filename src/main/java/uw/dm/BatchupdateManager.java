package uw.dm;

import java.util.List;

/**
 * 批量更新的管理类.
 */

public interface BatchupdateManager {

	/**
	 * 设置批量更新的数量.
	 * 
	 * @param size
	 *            批量更新的数量
	 */
	public void setBatchSize(int size);

	/**
	 * 获得批量更新的数量.
	 * 
	 * @return 获得批量更新的数量
	 */
	public int getBatchSize();

	/**
	 * 获得Batch的sql列表.
	 * 
	 * @return Batch的sql列表
	 */
	public List<String> getBatchList();

	/**
	 * 提交该事务.
	 * 
	 * @return 执行结果
	 * @throws TransactionException
	 *             事务异常
	 */
	public List<List<Integer>> submit() throws TransactionException;

}
