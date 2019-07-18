- ## 15.6 InnoDB On-Disk Structures

  - [15.6.1 Tables](https://dev.mysql.com/doc/refman/8.0/en/innodb-tables.html)

  - [15.6.2 Indexes](https://dev.mysql.com/doc/refman/8.0/en/innodb-indexes.html)

    #### 15.6.2.1 Clustered Indexes

    1. Typically, the clustered index is synonymous with the [primary key](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_primary_key).
  
    2. When you define a `PRIMARY KEY` on your table, `InnoDB` uses it as the clustered index.  If there is no logical unique and non-null column or set of columns, add a new [auto-increment](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_auto_increment) column, whose values are filled in automatically.
  
  3. If you do not define a `PRIMARY KEY` for your table, MySQL locates the first `UNIQUE` index where all the key columns are `NOT NULL` and `InnoDB` uses it as the clustered index.
  
  4. If the table has no `PRIMARY KEY` or suitable `UNIQUE` index, `InnoDB` internally generates a hidden clustered index named `GEN_CLUST_INDEX` on a synthetic column containing row ID values. The rows are ordered by the ID that `InnoDB` assigns to the rows in such a table. The row ID is a 6-byte field that increases monotonically as new rows are inserted. Thus, the rows ordered by the row ID are physically in insertion order.
  
    #### 15.6.2.1 Secondary Indexes
  
  1. All indexes other than the clustered index are known as [secondary indexes](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_secondary_index). 
  2. 在InnoDB中，辅助索引中的每个记录都包含该行的主键列以及为辅助索引指定的列。InnoDB使用此主键值搜索聚集索引中的行
  3. a secondary index = the primary key columns +  the columns specified for the secondary index。使用此primary key检索 clustered index中的行。
  4. 根据上述3来看，If the primary key is long, the secondary indexes use more space, so it is advantageous to have a short primary key.
  
- [15.6.3 Tablespaces](https://dev.mysql.com/doc/refman/8.0/en/innodb-tablespace.html)
  
- [15.6.4 Doublewrite Buffer](https://dev.mysql.com/doc/refman/8.0/en/innodb-doublewrite-buffer.html)
  
  - [15.6.5 Redo Log](https://dev.mysql.com/doc/refman/8.0/en/innodb-redo-log.html)
  
  - [15.6.6 Undo Logs](https://dev.mysql.com/doc/refman/8.0/en/innodb-undo-logs.html)
