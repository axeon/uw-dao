package uw.dao;

import java.util.HashSet;
import java.util.Set;

/**
 * 用于dao的save/update操作。 如果需要做save/update操作，则entity必须要实现该接口.
 *
 * @author axeon
 */
public interface DataEntity {

	/**
	 * 获得更改的字段列表.
	 *
	 * @return 字段列表
	 */
	public HashSet<String> GET_UPDATED_COLUMN();

	/**
	 * 得到变更信息.
	 *
	 * @return String
	 */
	public String GET_UPDATED_INFO();
}
