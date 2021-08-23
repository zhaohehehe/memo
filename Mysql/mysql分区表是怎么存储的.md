
## mysql分区表是怎么存储的？
```
CREATE TABLE `test_table` (
  `ID` varchar(100) NOT NULL,
  `TENANT_ID` varchar(250) DEFAULT NULL,
  `CREATE_TIME` int(11) NOT NULL DEFAULT '101',
  PRIMARY KEY (`ID`,`CREATE_TIME`),
  KEY `idx_1` (`TENANT_ID`),
	KEY `idx_2` (`TENANT_ID`, `CREATE_TIME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
/*!50100 PARTITION BY LIST (`CREATE_TIME`)
(PARTITION p1 VALUES IN (101) ENGINE = InnoDB,
 PARTITION p2 VALUES IN (102) ENGINE = InnoDB) */;
 
select partition_name,partition_description from information_schema.partitions where table_schema=schema() and table_name='test_table';

 
```
1. 分区索引
  - 对于非聚簇索引，无论索引是否带有分区字段，每个分区都单独维护一棵索引树。
  - 对于聚簇索引（GEN_CLUST_INDEX），只维护一棵索引树。

  ```
  -- mysql8查询INNODB_INDEXES、INNODB_INDEXES
  select * from information_schema.tables where table_schema='test_schema' and table_name='test_table';
  select * from information_schema.innodb_sys_tables where name like '%dp548700_new/test_table%';
  |TABLE_ID	 | NAME	| FLAG	| N_COLS	| SPACE	 | FILE_FORMAT	| ROW_FORMAT	| ZIP_PAGE_SIZE	| SPACE_TYPE

  select * from information_schema.innodb_sys_indexes where table_id = 'information_schema.innodb_sys_tables.table_id';
  |INDEX_ID	 | NAME	| TABLE_ID	| TYPE	| N_FIELDS	| PAGE_NO	| SPACE	| MERGE_THRESHOLD
  ```

2. truncate分区对索引是否有影响

  [Mysql  truncate 官方文档：](https://dev.mysql.com/doc/refman/8.0/en/truncate-table.html)
  > When used with partitioned tables, TRUNCATE TABLE preserves the partitioning; that is, the data and index files are dropped and re-created, while the partition definitions are unaffected.

  [Mysql  truncate partition 官方文档：](https://dev.mysql.com/doc/refman/8.0/en/alter-table-partition-operations.html)
  > TRUNCATE PARTITION merely deletes rows; it does not alter the definition of the table itself, or of any of its partitions.

3. Mysql分区表文件结构
  - 查看data路径：`show variables like '%datadir%';`
  - 可以通过执行DDL查看分区文件的变化。

4. MySQL 8.0中没FRM文件了还怎么恢复表的DDL信息？
  > By  Marco Tusa：
  > https://www.percona.com/blog/2018/12/07/mysql-8-frm-drop-how-to-recover-table-ddl/
  > <br>文中提及：分区表的SDI是个例外。分区表的SDI信息只存在于第一个分区，如果删除第一个分区，SDI信息会移动到下一个分区。

5. Mysql Purge
https://dev.mysql.com/doc/refman/8.0/en/innodb-purge-configuration.html
