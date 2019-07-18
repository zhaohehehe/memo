## 15.3 InnoDB Multi-Versioning

`InnoDB` is a [multi-versioned storage engine](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_mvcc):为每个变更行保存旧版本信息，用于事务 并发 和回滚。该信息在表空间中存储的数据结构为： [rollback segment](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_rollback_segment) 

rollback segment fields：

1. `DB_TRX_ID` (6-byte) 代表最后一次inserted（insert） or updated(update+delete)事务id。Also, a deletion is treated internally as an update where a special bit in the row is set to mark it as deleted. Each row also contains a 7-byte `DB_ROLL_PTR`field called the roll pointer. The roll pointer points to an undo log record written to the rollback segment. If the row was updated, the undo log record contains the information necessary to rebuild the content of the row before it was updated. A 6-byte `DB_ROW_ID` field contains a row ID that increases monotonically as new rows are inserted. If `InnoDB` generates a clustered index automatically, the index contains row ID values. Otherwise, the `DB_ROW_ID` column does not appear in any index.

2. `DB_ROLL_PTR`(7-byte) 滚动指针roll pointer。指向rollback segment的一个undo log record。

   **rollback segment**：insert  undo logs（事务回滚时需要，事务提交时丢弃） + update undo logs （事务回滚时需要，构建早期版本事务不存在时丢弃）

3.  `DB_ROW_ID`（6-byte）increases monotonically 单调递增。如果InnoDB自动生成聚集索引，则该索引包含行ID值。否则，db_row_id列不会出现在任何索引中。

   非聚集索引：记录的逻辑顺序和实际存储的物理顺序没有任何联系，指针记录

   聚集索引：索引中的数据物理存放地址和索引的顺序是一致的

### Multi-Versioning and Secondary Indexes

## 15.4 InnoDB Architecture

**Figure 15.1 InnoDB Architecture**

![InnoDB architecture diagram showing in-memory and on-disk structures.](https://dev.mysql.com/doc/refman/8.0/en/images/innodb-architecture.png)

### 15.5.1 Buffer Pool

1. 位于main memory,缓存表和索引数据：

   为了大容量read效率，buffer pool被划分为多个pages，每个page可以容纳多行。

   为了高效缓存管理，使用a linked list of pages管理。不常用数据使用LRU页置换算法;
2. **Figure 15.2 Buffer Pool List**
   
   ![Content is described in the surrounding text.](https://dev.mysql.com/doc/refman/8.0/en/images/innodb-buffer-pool-list.png)
   
   

### 15.5.2 Change Buffer

1. The change buffer is a special data structure that caches changes to [secondary index](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_secondary_index) pages when those pages are not in the [buffer pool](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_buffer_pool). The buffered changes, which may result from [`INSERT`](https://dev.mysql.com/doc/refman/8.0/en/insert.html), [`UPDATE`](https://dev.mysql.com/doc/refman/8.0/en/update.html), or [`DELETE`](https://dev.mysql.com/doc/refman/8.0/en/delete.html) operations (DML), are merged later when the pages are loaded into the buffer pool by other read operations.
2. **Figure 15.3 Change Buffer**

![Content is described in the surrounding text.](https://dev.mysql.com/doc/refman/8.0/en/images/innodb-change-buffer.png)

### 15.5.3 Adaptive Hash Index

根据观察到的搜索模式，使用索引键的前缀构建哈希索引。前缀可以是任何长度，并且可能只有B树中的一些值出现在哈希索引中。哈希索引是根据需要为经常访问的索引页构建的。

自适应哈希索引功能已分区。每个索引都绑定到一个特定的分区，并且每个分区都由一个单独的latch保护。

### 15.5.4 Log Buffer