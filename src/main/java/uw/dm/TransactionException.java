package uw.dm;

/**
 * 事务异常类。 几乎所有的数据库封装操作异常都通过这个类抛出.
 * @version 1.0
 */
public class TransactionException extends Exception {

	/**
	 * <code>serialVersionUID</code> 的注释.
	 */
	private static final long serialVersionUID = 8713460933603499992L;

	/**
	 * 构造函数.
	 */
	public TransactionException() {
		super();
	}
	/**
	 * 构造函数.
	 * @param msg 异常信息
	 */
	public TransactionException(String msg) {
		super(msg);
	}
	/**
	 * 构造函数.
	 * @param nestedThrowable Throwable对象
	 */
	public TransactionException(Throwable nestedThrowable) {
		super(nestedThrowable);
	}
	/**
	 * 构造函数.
	 * @param msg 异常信息
	 * @param nestedThrowable Throwable对象
	 */
	public TransactionException(String msg, Throwable nestedThrowable) {
		super(msg, nestedThrowable);
	}

}
