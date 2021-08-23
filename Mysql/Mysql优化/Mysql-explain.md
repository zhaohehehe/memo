
# Mysql explain
```
CREATE TABLE `lock_test` (
  `id` varchar(255) NOT NULL,
  `u_1` varchar(255) NOT NULL,
  `u_2` varchar(255) NOT NULL,
	`i_3` varchar(255) DEFAULT NULL,
	`i_4` varchar(255) DEFAULT NULL,
	`idx_1` varchar(255) DEFAULT NULL,
	`idx_2` varchar(255) DEFAULT NULL,
	`idx_3` varchar(255) DEFAULT NULL,
  `col1` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key` (`u_1`,`u_2`) USING BTREE,
	KEY `idx_key1` (`i_3`,`i_4`) USING BTREE,
	KEY `idx_key2` (`idx_1`,`idx_2`,`idx_3`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
DESC lock_test;
INSERT INTO `lock_test`(`id`, `u_1`, `u_2`, `i_3`, `i_4`, `idx_1`, `idx_2`, `idx_3`, `col1`) VALUES ('001', '001', '001', '001', '001', '001', '001', '001', '001');
INSERT INTO `lock_test`(`id`, `u_1`, `u_2`, `i_3`, `i_4`, `idx_1`, `idx_2`, `idx_3`, `col1`) VALUES ('002', '001', '002', '002', '002', '002', '002', '002', '002');
INSERT INTO `lock_test`(`id`, `u_1`, `u_2`, `i_3`, `i_4`, `idx_1`, `idx_2`, `idx_3`, `col1`) VALUES ('005', '005', '005', '005', '005', '005', '005', '005', '005');
INSERT INTO `lock_test`(`id`, `u_1`, `u_2`, `i_3`, `i_4`, `idx_1`, `idx_2`, `idx_3`, `col1`) VALUES ('009', '009', '009', '009', '009', '009', '009', '009', '009');

```

## id（表的执行顺序）
This is the `sequential number` of the SELECT within the query. <br>
When `M union N`,id can be `NULL`,table=<unionM,N>.
+ id相同，自上而下执行
+ id不同，id值越大，优先级越高，越先执行。

## select_type
+ `SIMPLE`	None	Simple SELECT (not using UNION or subqueries)
+ `PRIMARY`	None	Outermost SELECT(最外侧子查询)
+ `UNION`	None	Second or later SELECT statement in a UNION
+ DEPENDENT UNION	dependent (true)	Second or later SELECT statement in a UNION, dependent on outer query
+ UNION RESULT	union_result	Result of a UNION.
+ `SUBQUERY`	None	First SELECT in subquery
+ DEPENDENT SUBQUERY	dependent (true)	First SELECT in subquery, dependent on outer query
+ `DERIVED`	None	Derived table.`<derivedN>`
+ DEPENDENT DERIVED	dependent (true)	Derived table dependent on another table
+ MATERIALIZED	materialized_from_subquery	Materialized subquery
+ UNCACHEABLE SUBQUERY	cacheable (false)	A subquery for which the result cannot be cached and must be re-evaluated for each row of the outer query
+ UNCACHEABLE UNION	cacheable (false)	The second or later select in a UNION that belongs to an uncacheable subquery (see UNCACHEABLE SUBQUERY)

## table ``<unionM,N>、<derivedN>、<subqueryN

## partitions

## [type](https://dev.mysql.com/doc/refman/8.0/en/explain-output.html#explain-join-types)
+ `system`
+ `const`
  - used when you compare `all parts of a PRIMARY KEY or UNIQUE index` to constant values.
  ```
  SELECT * FROM tbl_name WHERE primary_key=1;
  SELECT * FROM tbl_name WHERE primary_key_part1=1 AND primary_key_part2=2;
  ```
+ `eq_ref`(唯一索引)
  - used when `all parts of an index are used by the join and the index is a PRIMARY KEY or UNIQUE NOT NULL index`
+ `ref`(非唯一索引)
  -  used for indexed columns that are compared using the `= or <=>` operator.  
  - is used if the join uses only a `leftmost prefix of the key` or if `the key is not a PRIMARY KEY or UNIQUE index(普通索引)`(if the join cannot select a single row based on the key value)
+ fulltext
+ ref_or_null
+ index_merge
+ unique_subquery
+ index_subquery
+ `range`
  - can be used when a `key column` is compared to a `constant using` any of the =, <>, >, >=, <, <=, IS NULL, <=>, BETWEEN, LIKE, or IN() operators.
  ```
  WHERE key_column BETWEEN 10 and 20;
  WHERE key_column IN (10,20,30);
  WHERE key_part1 = 10 AND key_part2 IN (10,20,30);
  ```
+ index
  - The index join type is the `same` as ALL, `except` that the `index tree is scanned`(select `columns` from table_name)，如果`columns`刚好和创建的索引`index_cols`一致，那么`index_cols`会`覆盖the selected columns`。
  ```
  explain select * from lock_test;
  explain select `u_1`,`u_2` from lock_test
  ```
+ ALL

## [possible_keys]()

## [key]()
- [Index Hints,强制MySQL使用或者不使用possible_keys中存在的索引](https://dev.mysql.com/doc/refman/8.0/en/index-hints.html)

## [key_len]()
  - indicates the `length of the key` that MySQL decided to use(表结构`定义`的长度).
  - determine how many `parts of a multiple-part` key MySQL actually uses. If the key column says NULL, the key_len column also says NULL.
  - Due to the key storage format, the key length is `one greater` for a column that can be NULL than for a NOT NULL column.
  - 相同查询result的情况下，key_len越小越好。


  ```
  -- key_len=767
  explain select `u_1`,`u_2` from lock_test where `u_1`='001';
  -- key_len=767*2
  explain select `u_1`,`u_2` from lock_test where `u_1`='001' and `u_2`='002';
  -- key_len=768
  explain select `i_3`,`i_4` from lock_test where `i_3`='002';
  explain select `i_3`,`i_4` from lock_test where `i_3`='002' and `i_4`='002';

  ```
## ref
  - shows `which columns or constants` are compared to the index named in the key column to select rows from the table

## rows
-  indicates the number of rows MySQL believes it must examine to execute the query***（越小越好）***
- For InnoDB tables, this number is an `estimate`, and may not always be exact.

## filtered
  - The `filtered` column indicates an `estimated percentage` of table rows that are filtered by the table condition. ***(返回行数(joined)占读取(examined)行数的百分比，越大表示过滤效果越好)***
  - `rows` shows the `estimated number` of rows examined and `rows × filtered` shows the number of rows that are joined with the following table.
  ```
  For example, if rows is 1000 and filtered is 50.00 (50%), the number of rows to be joined with the following table is 1000 × 50% = 500.
  ```

## [Extra](https://dev.mysql.com/doc/refman/8.0/en/explain-output.html#explain-extra-information)
  - This column contains `additional information` about how MySQL resolves the query. For descriptions of the different values, see EXPLAIN Extra Information.
  - `Using_filesort(无法利用索引完成排序，需要优化)` [order by](https://dev.mysql.com/doc/refman/8.0/en/order-by-optimization.html)
  ```
explain select * from lock_test where `idx_1`='002' order by idx_3;-- Using filesort
explain select * from lock_test where `idx_1`='002'order by idx_1;
explain select * from lock_test where `idx_1`='002'order by idx_2;
explain select * from lock_test where `idx_1`='002'order by idx_1,idx_2;
explain select * from lock_test where `idx_1`='002'order by idx_2, idx_3;
explain select * from lock_test where `idx_1`='002'order by idx_1,idx_2, idx_3;
-- Using filesort
explain SELECT * FROM lock_test ORDER BY i_3 DESC, i_4 DESC;
-- Using filesort
explain SELECT * FROM lock_test ORDER BY i_3 ASC, i_4 ASC;
-- Using filesort
explain SELECT * FROM lock_test ORDER BY i_3 ASC, i_4 DESC;
-- Using filesort
explain SELECT * FROM lock_test ORDER BY i_3 DESC, i_4 ASC;
-- Backward index scan; Using index
explain SELECT i_3,i_4 FROM lock_test ORDER BY i_3 DESC, i_4 DESC;
-- Using index
explain SELECT i_3,i_4 FROM lock_test ORDER BY i_3 ASC, i_4 ASC;
-- Using index; Using filesort
explain SELECT i_3,i_4 FROM lock_test ORDER BY i_3 ASC, i_4 DESC;
-- Using index; Using filesort
explain SELECT i_3,i_4 FROM lock_test ORDER BY i_3 DESC, i_4 ASC;
  ```
  - `Using temporary(使用临时表，需要优化，常见于GROUP BY and ORDER BY)`

  - `Using index`

  - `Using where`

  - `Using index condition`
    + ICP特性是用来`减少全表读和磁盘IO`的，对于InnoDB的聚簇索引，由于记录都加载到内存，所以适用ICP特性不能减少IO.（select @@optimizer_switch。SET optimizer_switch = 'index_condition_pushdown=on';）
    + [参考](https://www.zkii.net/tech/php/2917.html)

***

***

## Mysql 聚集索引的选择
https://dev.mysql.com/doc/refman/8.0/en/innodb-index-types.html

1. 具体可以通过：SHOW INDEX FROM 数据表，的Cardinality的值来判断：
Cardinality表示索引列的唯一值的估计数量，如果跟数据行的数量接近，则说明该列存在的重复值少，列的过滤性较好；如果相差太大，即Cardinality / 数据行总数，的值太小，如性别列只包含“男”，“女”两个值，则说明该列存在大量重复值，需要考虑是否删除该索引。

2. 从性能角度考虑，使用UUID来做聚簇索引会很糟糕，它使得聚簇索引的插入变得完全随机，这是最坏的情况，是的数据没有任何聚集的特性。总结下使用类似UUID这种随机的聚簇索引的缺点：
  - 1.UUID字段长，索引占用的空间更大。
  - 2.写入是乱序的，InnoDB不得不频繁的做页分裂操作，以便新的行分配空间，页分裂会导致移动大量数据，一次插入最少需要修改三个页而不是一个页。
  - 3.写入的目标页可能已经刷到磁盘上并从缓存中移除，或者还没有被加载到缓存中，InnoDB在插入之前不得不先找到并从磁盘读取目标页到内存中，这将导致大量的随机IO。
  - 4.频繁的页分裂，页会变的稀疏并被不规则的填充，会产生空间碎片。
