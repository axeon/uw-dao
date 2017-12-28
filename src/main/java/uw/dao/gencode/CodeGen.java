package uw.dao.gencode;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.dao.util.DaoStringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

/**
 * 代码生成入口.
 * 
 * @author axeon
 */
public class CodeGen {

	/**
	 * 日志.
	 */
	private static final Logger logger = LoggerFactory.getLogger(CodeGen.class);

	/**
	 * 文件编码.
	 */
	public static final String SYSTEM_ENCODING = "UTF-8";

	/**
	 * 源代码的路径.
	 */
	public static final String SOURCECODE_PATH = "d:/";

	/**
	 * 包名
	 */
	public static final String PACKAGE_NAME = "";
	/**
	 * 数据库方言 MYSQL or ORACLE
	 */
	public static String DATABSE_DIALECT = "MYSQL";

	/**
	 * 连接池名称，不设置为默认连接
	 */
	public static final String CONN_NAME = "";
	/**
	 * 指定要生成的表的信息，多个表名用","分割
	 */
	public static final String TABLE_LIST = "";
	/**
	 * 数据库连接模式 -> ORACLE需要设置,默认取数据库连接名称
	 */
	public static final String CONN_SCHEMA = "";

	/**
	 * TableMetaInterface对象
	 */
	private static TableMetaInterface tableMetaInterface;

	/**
	 * logger.error.
	 * 
	 * @param msg
	 *            信息
	 */
	private static void onError(String msg) {
		logger.error("msg");
		System.exit(-1);
	}

	/**
	 * 开始代码生成.
	 * 
	 * @throws Exception
	 *             异常
	 */
	public static void genCode() throws Exception {
		// 参数CHECK
		if (DATABSE_DIALECT == null || DATABSE_DIALECT.equals("")) {
			onError("请设置数据库方言: MYSQL or ORCLE");
		}
		DATABSE_DIALECT = DATABSE_DIALECT.toUpperCase();
		if (CONN_NAME == null) {
			onError("连接池名不能为null");
		}
		if (DATABSE_DIALECT.equals("MYSQL")) {
			tableMetaInterface = new MySQLDataMetaImpl(CONN_NAME);
		} else if (DATABSE_DIALECT.equals("ORACLE")) {
			tableMetaInterface = new OracleDataMetaImpl(CONN_NAME, CONN_SCHEMA);
		}
		logger.info("开始代码生成...");
		logger.info("CONN_NAME={}", CONN_NAME);
		logger.info("SOURCECODE_PATH={}", SOURCECODE_PATH);
		logger.info("PACKAGE_NAME={}", PACKAGE_NAME);
		logger.info("SYSTEM_ENCODING={}", SYSTEM_ENCODING);
		init();
		process();
		logger.info("代码生成完成...");
		System.exit(-1);
	}

	/**
	 * Configuration对象.
	 */
	private static Configuration cfg;

	/**
	 * 初始化模板配置.
	 * 
	 * @throws Exception
	 *             异常
	 */
	private static void init() {
		// 初始化FreeMarker配置
		// 创建一个Configuration实例
		cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
		// 设置FreeMarker的模版文件位置
		cfg.setClassForTemplateLoading(CodeGen.class, "/uw/dao/gencode/"); // 类路径
	}

	/**
	 * 预处理信息.
	 * 
	 * @throws Exception
	 *             异常
	 */
	private static void process() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String author = "axeon";
		map.put("author", author);
		map.put("date", new Date());
		map.put("package", PACKAGE_NAME);
		HashSet<String> set = new HashSet<String>();
		if (TABLE_LIST != null && !TABLE_LIST.equals("")) {
			String[] ts = TABLE_LIST.split(",");
			for (String t : ts) {
				set.add(t);
			}
		}
		List<MetaTableInfo> tablelist = tableMetaInterface.getTablesAndViews(set);
		for (MetaTableInfo tmeta : tablelist) {
			map.put("tableMeta", tmeta);
			// 获得主键列表
			List<MetaPrimaryKeyInfo> pklist = tableMetaInterface.getPrimaryKey(tmeta.getTableName());
			map.put("pkList", pklist);

			// 获得列列表
			List<MetaColumnInfo> columnlist = tableMetaInterface.getColumnList(tmeta.getTableName(), pklist);
			map.put("columnList", columnlist);

			String fileName = DaoStringUtils.toUpperFirst(tmeta.getEntityName()) + ".java";
			String savePath = SOURCECODE_PATH + "/" + PACKAGE_NAME.replaceAll("\\.", "/") + "/";
			Template template = cfg.getTemplate("entity.ftl");
			buildTemplate(template, map, savePath, fileName);
		}
	}

	/**
	 * 生成代码.
	 * 
	 * @param template
	 *            Template对象
	 * @param root
	 *            Map对象
	 * @param savePath
	 *            保存路径
	 * @param fileName
	 *            文件名字
	 */
	@SuppressWarnings("rawtypes")
	private static void buildTemplate(Template template, Map root, String savePath, String fileName) {
		String realFileName = savePath + fileName;
		File newsDir = new File(savePath);
		if (!newsDir.exists()) {
			newsDir.mkdirs();
		}
		try {
			Writer out = new OutputStreamWriter(new FileOutputStream(realFileName), SYSTEM_ENCODING);
			template.process(root, out);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
