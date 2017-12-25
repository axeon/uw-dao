package uw.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackTraceTools {

	private static final Logger logger = LoggerFactory.getLogger(StackTraceTools.class);

	/**
	 * key=异常名称 value=代码行数
	 */
	private ArrayList<String[]> map = new ArrayList<String[]>();

	/**
	 * 读入文件内容
	 * 
	 * @param filename
	 * @param charset
	 * @return
	 */
	public void analysisFile(String filename, String charset) {
		String exception = "";
		String stacktrace = "";
		try {
			File ff = new File(filename);
			InputStreamReader read = new InputStreamReader(new FileInputStream(ff), charset);
			BufferedReader ins = new BufferedReader(read);
			String dataLine = "";
			while (null != (dataLine = ins.readLine())) {
				if (dataLine.length() < 1)
					continue;
				if (dataLine.charAt(0) != '\t') {
					if (dataLine.indexOf("Exception") == -1 && dataLine.indexOf("Error") == -1) {
						continue;
					}

					// 此时说明是异常，放入map中。
					map.add(new String[] { exception, stacktrace });
					exception = dataLine.trim();
					stacktrace = "";
					logger.debug("正在处理异常" + exception);
				} else {
					if (dataLine.indexOf("at oracle.") == -1 && dataLine.indexOf("at com.caucho.") == -1
							&& dataLine.indexOf("at com.mysql.") == -1 && dataLine.indexOf("at sun.reflect.") == -1
							&& dataLine.indexOf("at com.mysql.") == -1 && dataLine.indexOf("at java.util.") == -1
							&& dataLine.indexOf("at java.net.") == -1 && dataLine.indexOf("at uw.dao.") == -1
							&& dataLine.indexOf("at java.lang.Thread") == -1) {
						stacktrace += dataLine + "|";
					}
				}
			}
			ins.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {

		}
	}

	/**
	 * 写回文件
	 * 
	 * @param filename
	 * @param charset
	 */
	public void writeFile(String filename, String charset) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename), charset);
			BufferedWriter br = new BufferedWriter(osw);
			for (String[] data : map) {
				if (data[1].length() > 1) {
					br.write("\"" + data[0] + "\"" + "," + "\"" + data[1] + "\"" + "\n");
				}
			}
			br.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 构造器
	 * 
	 * @param inputFile
	 *            要处理的文件
	 * @param incharset
	 *            读取的编码
	 * @param outcharset
	 *            输出的编码
	 * @param outputFile
	 *            输出的文件
	 */
	public StackTraceTools(String inputFile, String incharset, String outputFile, String outcharset) {

		long start = System.currentTimeMillis();
		logger.info("开始处理" + inputFile + "文件！");
		analysisFile(inputFile, incharset);
		logger.info("读取编码:" + incharset);
		logger.info("输出" + outputFile + "文件！");
		writeFile(outputFile, outcharset);
		logger.info("输出编码:" + outcharset);
		logger.info("全部处理完毕！" + (System.currentTimeMillis() - start));
	}

	public static void main(String[] args) {
		new StackTraceTools("D:/Terry.txt", "utf-8", "D:/out.csv", "gbk");
	}

}
