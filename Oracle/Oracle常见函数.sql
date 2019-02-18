-- "REGEXP_SUBSTR"(source, pattern, pos)
-- 10g新增函数
	SELECT "REGEXP_SUBSTR"('1432@zhaohe.com.cn', '@.**', 1) FROM dual;-- @zhaohe.com.cn