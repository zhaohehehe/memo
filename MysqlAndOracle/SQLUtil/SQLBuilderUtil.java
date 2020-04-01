package test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

public class SQLBuilderUtil {

	public static String filterDebugSql(String preparedSql) {
		return preparedSql.replaceAll("[?]", "'{}'");
	}

	public static String filterPage(String targetSelectSql) {
		if (isOracleEnv()) {
			return " from (select target.*,ROWNUM rn from (" + targetSelectSql
					+ ") target where ROWNUM <= ?) result where rn > ?";
		}
		if (isMysqlEnv()) {
			return " from (" + targetSelectSql + ") limit ?,?";
		}
		return targetSelectSql;
	}

	private static boolean isMysqlEnv() {
		return false;
	}

	private static boolean isOracleEnv() {
		return false;
	}

	public static String filterColumn(String tableAlias, String columnName, int count) {
		Assert.isTrue(StringUtils.isNotBlank(columnName), "未指定column name！");
		Assert.isTrue(count > 0, columnName + " count <= 0 ！");
		StringBuilder filter = new StringBuilder(" ");
		if (StringUtils.isNotBlank(tableAlias)) {
			filter.append(tableAlias).append(".");
		}
		filter.append(columnName);
		if (count == 1) {
			filter.append(" = ? ");
		} else {
			filter.append(" in(?");
			for (int i = 1; i < count; i++) {
				filter.append(",?");
			}
			filter.append(") ");
		}
		return filter.toString();
	}

	public static void main(String[] args) {
		List<String> list = SQLBuilderUtil.rollingMMDDBaseNow("yyyy-MM-dd", "102", -5);
		for (String ele : list) {
			System.out.println(ele);
		}
		System.out.println("------------------------");
		list = SQLBuilderUtil.rollingMMDDBaseNow("YYYY-MM-dd", "102", -5);
		for (String ele : list) {
			System.out.println(ele);
		}
	}

	public static List<String> rollingMMDDBaseNow(String tartgetFormat, String mmdd, int amount) {
		if (tartgetFormat.contains("yyyy-MM-dd")) {
			System.out.println("按照天跨年");
		}
		if (tartgetFormat.contains("YYYY-MM-dd")) {
			System.out.println("按照周跨年");
		}
		Assert.isTrue(mmdd.matches("^\\d{3,4}$"), mmdd + " is an invalid mmdd");
		List<String> gcts = new ArrayList<>();
		gcts.add(mmdd);
		int length = mmdd.length();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, Integer.valueOf(mmdd.substring(0, length - 2)) - 1);
		calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(mmdd.substring(length - 2)));
		int count = Math.abs(amount);
		for (int i = 0; i < count; i++) {
			calendar.add(Calendar.DAY_OF_YEAR, amount / count);
			String newMmdd = new SimpleDateFormat(tartgetFormat, Locale.ENGLISH).format(calendar.getTime());
			if (!gcts.contains(newMmdd)) {
				gcts.add(newMmdd);
			}
		}
		return gcts;

	}

	public static String filterIn(int count, int subCount) {
		StringBuilder placeHolders = new StringBuilder();
		if (subCount > 1) {
			placeHolders.append("?");
			for (int i = 1; i < subCount; i++) {
				placeHolders.append(",?");
			}
		} else {
			placeHolders.append("?");
		}
		StringBuilder ins = new StringBuilder("(");
		if (count >= 1) {
			ins.append("(").append(placeHolders.toString()).append(")");
			for (int i = 1; i < count; i++) {
				ins.append(",(").append(placeHolders.toString()).append(")");
			}
		}
		ins.append(")");
		return ins.toString();

	}
}
