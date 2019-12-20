package uw.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 组合了互联网应用常见列表所需数据的集合接口。 实现了iterator,Iterable.
 *
 * @author axeon
 * @param <T>
 *            映射的类型
 */
public class DataList<T> implements Iterator<T>, Iterable<T>, Serializable {

	/**
	 * 序列号id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 开始的索引.
	 */
	@JsonProperty
	private int startIndex = 0;

	/**
	 * 返回的结果集大小.
	 */
	@JsonProperty
	private int resultNum = 0;

	/**
	 * List大小（实际返回的结果集大小）.
	 */
	@JsonProperty
	private int size = 0;

	/**
	 * 整个表数据量大小.
	 */
	@JsonProperty
	private int sizeAll = 0;

	/**
	 * 当前页.
	 */
	@JsonProperty
	private int page = 0;

	/**
	 * 总页数.
	 */
	@JsonProperty
	private int pageCount = 0;

	/**
	 * 当前索引.
	 */
	private transient int currentIndex = -1;

	/**
	 * 返回的value object数组.
	 *
	 * @param <T>
	 *            映射的类型
	 */
	@JsonProperty
	private ArrayList<T> results = null;

	/**
	 * 构造函数.
	 */
	public DataList() {
		super();
	}

	/**
	 * DataList构造器.
	 *
	 * @param results
	 *            结果集
	 * @param startIndex
	 *            开始位置
	 * @param resultNum
	 *            每页大小
	 * @param allSize
	 *            所有的数量
	 */
	public DataList(ArrayList<T> results, int startIndex, int resultNum, int allSize) {
		this.startIndex = startIndex;
		this.results = results;
		this.resultNum = resultNum;
		this.size = this.results.size();
		this.sizeAll = allSize;

		if (this.sizeAll > 0 && this.resultNum > 0) {
			// 计算当前页
			this.page = (int) Math.ceil((double) startIndex / (double) resultNum);
			// 计算总页数
			this.pageCount = (int) Math.ceil((double) sizeAll / (double) resultNum);
		}
	}

    /**
     * 计算页面参数信息。
     */
    public void calcPages(int sizeAll) {
        this.sizeAll = sizeAll;
        if (this.sizeAll > 0 && this.resultNum > 0) {
            // 计算当前页
            this.page = (int) Math.ceil((double) startIndex / (double) resultNum);
            // 计算总页数
            this.pageCount = (int) Math.ceil((double) sizeAll / (double) resultNum);
        }
    }


	/**
	 * 定位到某条位置.
	 *
	 * @param index
	 *            位置
	 */
	public void absolute(int index) {
		this.currentIndex = index - 1;
	}

	/**
	 * 获得指定处的对象.
	 *
	 * @param index
	 *            位置
	 * @return value object数组指定的对象
	 */
	public T get(int index) {
		return results.get(index);
	}

	/**
	 * 是否有下一条记录.
	 *
	 * @return 是否有下一条记录
	 */
	@Override
	public boolean hasNext() {
		return (currentIndex + 1 < this.size);
	}

	/**
	 * 获取下一条记录.
	 *
	 * @return 下一条记录
	 */
	@Override
	public T next() {
		if (currentIndex < this.size) {
			currentIndex++;
		}
		return results.get(currentIndex);
	}

	/**
	 * 是否有上一条记录.
	 *
	 * @return 是否有上一条记录
	 */
	public boolean hasPrevious() {
		return (currentIndex > 0);
	}

	/**
	 * 获取上一条记录.
	 *
	 * @return 上一条记录
	 */
	public T previous() {
		if (currentIndex > -1) {
			currentIndex--;
		}
		return results.get(currentIndex);
	}

	/**
	 * 为了兼容List接口，不实现，不起作用。 抛出异常.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取当前List大小.
	 *
	 * @return 当前List大小
	 */
	public int size() {
		return this.size;
	}

	/**
	 * 获取该表/视图所有的数据大小.
	 *
	 * @return 该表/视图所有的数据大小
	 * @throws TransactionException
	 *             事务异常
	 */
	public int sizeAll() {
		return this.sizeAll;
	}

	/**
	 * 按照总记录数和每页条数计算出页数.
	 *
	 * @return 页数
	 */
	public int pageCount() {
		return this.pageCount;
	}

	/**
	 * 当前页.
	 *
	 * @return 页数
	 */
	public int page() {
		return this.page;
	}


	/**
	 * 在整个数据集中的开始索引位置.
	 *
	 * @return 开始位置
	 */
	public int startIndex() {
		return this.startIndex;
	}

	/**
	 * 返回结果集大小.
	 *
	 * @return int
	 */
	public int resultNum() {
		return this.resultNum;
	}

	/**
	 * 返回该结果集.
	 *
	 * @return 结果集
	 */
	public ArrayList<T> results() {
		return this.results;
	}

	/**
	 * 重新设定结果集合.
	 *
	 * @param objects
	 *            objects集合
	 */
	public void reset(ArrayList<T> objects) {
		this.results = objects;
		this.currentIndex = -1;
		if (this.size != objects.size()) {
			this.size = objects.size();
		}
	}

	/**
	 * 获得iterator列表.
	 *
	 * @return Iterator列表
	 */
	@Override
	public Iterator<T> iterator() {
		return this;
	}

}
