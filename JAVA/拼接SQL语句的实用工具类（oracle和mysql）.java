package com.zhaohe.util;

import java.io.Serializable;

public class SQLUtil {
	public static void main(String[] args) {
		System.out.println(SQLUtil.valid("20199-03-02 12:10:09"));
		System.out.println(SQLUtil.oracle_time_minus.process("standard.plan_end_time", "standard.plan_execute_time",
				"hh24:mi:ss"));
		System.out.println(SQLUtil.mysql_time_minus.process("standard.plan_end_time", "standard.plan_execute_time"));
		System.out.println(SQLUtil.mysql_bitwise_or.process("server_node_id", "4"));
		System.out.println(SQLUtil.oracle_bitwise_or.process("server_node_id", "4"));
	}

	private static final String DATE_REGEX = "((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))"
			+ "|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)"
			+ "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))"
			+ "|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))";
	private static final String TIME_REGEX = "(([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]))";
	private static final String DATE_TIME_REGEX = "\\s*" + DATE_REGEX + "\\s*" + TIME_REGEX + "\\s*";
	// private final static String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 例如：2019-03-13 09:46:00->313,2018-10-13 09:46:00->1013
	 */
	public static final String MDD_FORMAT = "Mdd";
	/**
	 * 例如：2019-03-13 09:46:00->20190313
	 */
	public static final String YYYYMMDD_FORMAT = "yyyyMMdd";

	/**
	 * 例如：2019-03-13 09:46:00->13,2018-10-03 09:46:00->03
	 */
	public static final String DD_FORMAT = "dd";

	/**
	 * sql相关函数
	 */
	public static final String ORACLE_UUID = " sys_guid() ";
	public static final String MYSQL_UUID = " uuid() ";

	/**
	 * oracle批量分隔符
	 */
	public static final char ORACLE_BATCH_SPLIT = ' ';
	/**
	 * mysql批量分隔符
	 */
	public static final char MYSQL_BATCH_SPLIT = ',';

	public static final String[] ORACLE_TO_CHAR_FUNCTION = new String[] { " to_char(", ") " };
	public static final String[] MYSQL_TO_CHAR_FUNCTION = new String[] { " cast(", " as char) " };

	public static final String[] ORACLE_TO_DATE_FUNCTION = new String[] { " to_date(", ") " };
	/**
	 * oracle保留0位小数
	 */
	public static final String[] ORACLE_ROUND0_FUNCTION = new String[] { " round(", ",0) " };
	/**
	 * oracle保留2位小数
	 */
	public static final String[] ORACLE_ROUND2_FUNCTION = new String[] { " round(", ",2) " };
	/**
	 * mysql时间转化为秒
	 */
	public static final String[] MYSQL_TIME_TO_SEC_FUNCTION = new String[] { " time_to_sec(", ") " };

	/**
	 * oracle位运算sql
	 */
	public static final OperatorOnTwo oracle_bitwise_or = new OperatorOnTwo() {
		private static final long serialVersionUID = 1L;

		@Override
		public String process(String aTimeField, String bTimeField) {
			return new StringBuilder(" ").append(aTimeField).append("+").append(bTimeField).append("-")
					.append("bitand(").append(aTimeField).append(",").append(bTimeField).append(")").append(" ")
					.toString();
		}
	};
	/**
	 * mysql位运算sql
	 */
	public static final OperatorOnTwo mysql_bitwise_or = new OperatorOnTwo() {
		private static final long serialVersionUID = 1L;

		@Override
		public String process(String aTimeField, String bTimeField) {
			return new StringBuilder(" ").append(aTimeField).append("|").append(bTimeField).append(" ").toString();
		}
	};

	/**
	 * mysql返回时间差值sql(单位：秒)
	 */
	public static final OperatorOnTwo mysql_time_minus = new OperatorOnTwo() {
		private static final long serialVersionUID = 1L;

		@Override
		public String process(String aTimeField, String bTimeField) {
			return new StringBuilder(" ").append(MYSQL_TIME_TO_SEC_FUNCTION[0]).append(aTimeField)
					.append(MYSQL_TIME_TO_SEC_FUNCTION[1]).append("-").append(MYSQL_TIME_TO_SEC_FUNCTION[0])
					.append(bTimeField).append(MYSQL_TIME_TO_SEC_FUNCTION[1]).append(" ").toString();
		}
	};

	/**
	 * oracle返回时间差值sql(单位：秒)
	 */
	public static final OperatorOnThree oracle_time_minus = new OperatorOnThree() {
		private static final long serialVersionUID = 1L;

		@Override
		public String process(String aTimeField, String bTimeField, String format) {
			return new StringBuilder(" ").append(ORACLE_ROUND0_FUNCTION[0]).append("(")
					.append(ORACLE_TO_DATE_FUNCTION[0]).append(aTimeField).append(",").append("'").append(format)
					.append("'").append(ORACLE_TO_DATE_FUNCTION[1]).append("-").append(ORACLE_TO_DATE_FUNCTION[0])
					.append(bTimeField).append(",").append("'").append(format).append("'")
					.append(ORACLE_TO_DATE_FUNCTION[1]).append(")").append("*86400").append(ORACLE_ROUND0_FUNCTION[1])
					.append(" ").toString();
		}
	};

	public static boolean valid(String str) {
		if (str.matches(DATE_TIME_REGEX)) {
			return true;
		}
		return false;
	}

	public interface OperatorOnTwo extends Serializable {
		/**
		 * 
		 * @param aTimeField
		 * @param bTimeField
		 * @return
		 */
		public String process(String aTimeField, String bTimeField);
	}

	public interface OperatorOnThree extends Serializable {
		/**
		 * 
		 * @param aTimeField
		 * @param bTimeField
		 * @param format
		 *            该参数对应于aTimeField以及bTimeField的数据库format
		 * @return
		 */
		public String process(String aTimeField, String bTimeField, String format);
	}

}
