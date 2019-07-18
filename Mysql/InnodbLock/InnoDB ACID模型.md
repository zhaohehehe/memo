### 15.1.2 Best Practices for InnoDB Tables

This section describes best practices when using `InnoDB` tables.

- Specifying a [primary key](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_primary_key) for every table using the most frequently queried column or columns, or an [auto-increment](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_auto_increment) value if there is no obvious primary key.在频繁查询的列上设置主键可加快性能。

- 多表关联使用[joins](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_join) .为了提高性能，可以在join columns上设置 [foreign keys](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_foreign_key) . foreign keys是被indexed的。

- Turning off [autocommit](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_autocommit). Committing hundreds of times a second puts a cap on performance (limited by the write speed of your storage device).

- Grouping sets of related [DML](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_dml) operations into [transactions](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_transaction), by bracketing them with `START TRANSACTION` and `COMMIT` statements. While you don't want to commit too often, you also don't want to issue huge batches of [`INSERT`](https://dev.mysql.com/doc/refman/8.0/en/insert.html), [`UPDATE`](https://dev.mysql.com/doc/refman/8.0/en/update.html), or [`DELETE`](https://dev.mysql.com/doc/refman/8.0/en/delete.html) statements that run for hours without committing.（如果sql语句比较多不想频繁commit，也不希望执行很长时间又不提交，那么可以使用事务）。

- Not using [`LOCK TABLES`](https://dev.mysql.com/doc/refman/8.0/en/lock-tables.html) statements. `InnoDB` can handle multiple sessions all reading and writing to the same table at once, without sacrificing reliability or high performance. To get exclusive write access to a set of rows, use the [`SELECT ... FOR UPDATE`](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking-reads.html) syntax to lock just the rows you intend to update.（要获得对一组行的独占写入权限，请使用SELECT ... FOR UPDATE语法）。

- Enabling the [`innodb_file_per_table`](https://dev.mysql.com/doc/refman/8.0/en/innodb-parameters.html#sysvar_innodb_file_per_table) option or using general tablespaces to put the data and indexes for tables into separate files, instead of the [system tablespace](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_system_tablespace).

  The [`innodb_file_per_table`](https://dev.mysql.com/doc/refman/8.0/en/innodb-parameters.html#sysvar_innodb_file_per_table) option is enabled by default.

- Evaluating whether your data and access patterns benefit from the `InnoDB` table or page [compression](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_compression) features. You can compress `InnoDB` tables without sacrificing read/write capability.

- Running your server with the option [`--sql_mode=NO_ENGINE_SUBSTITUTION`](https://dev.mysql.com/doc/refman/8.0/en/server-system-variables.html#sysvar_sql_mode) to prevent tables being created with a different storage engine if there is an issue with the engine specified in the `ENGINE=` clause of [`CREATE TABLE`](https://dev.mysql.com/doc/refman/8.0/en/create-table.html)

## 15.2 InnoDB and the ACID Model

### Atomicity

The **atomicity** aspect of the ACID model mainly involves `InnoDB` [transactions](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_transaction). Related MySQL features include:

- Autocommit setting.
- [`COMMIT`](https://dev.mysql.com/doc/refman/8.0/en/commit.html) statement.
- [`ROLLBACK`](https://dev.mysql.com/doc/refman/8.0/en/commit.html) statement.
- Operational data from the `INFORMATION_SCHEMA` tables.

### Consistency

The **consistency** aspect of the ACID model mainly involves internal `InnoDB` processing to protect data from crashes. Related MySQL features include:

- `InnoDB` [doublewrite buffer](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_doublewrite_buffer). 一种file flush技术，当pages写入数据文件之前，会先写入一个连续的区域，这个区域叫做doublewrite buffer。doublewrite buffer完成之后，才能写入数据文件。（先把磁盘上的数据加载到内存中，在内存中对数据进行修改，再刷回磁盘上）
- `InnoDB` [crash recovery](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_crash_recovery).

### Isolation

The **isolation** aspect of the ACID model mainly involves `InnoDB` [transactions](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_transaction), in particular the [isolation level](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_isolation_level) that applies to each transaction. Related MySQL features include:

- [Autocommit](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_autocommit) setting.
- `SET ISOLATION LEVEL` statement.
  1. **SERIALIZABLE**
  
     上面三个隔离级别对同一条记录的读和写都可以并发进行，但是串行化格式下就只能进行读-读并发。只要有一个事务操作一条记录的写，那么其他要访问这条记录的事务都得等着。
  
     ![img](https://pics3.baidu.com/feed/cb8065380cd7912352da984a787cce86b2b780a7.jpeg?token=0cea6b56b279af02fde8ec0ce6e5a1bf&s=F39E4322665AC92848F029C3020010BA)
  
  2. **REPEATABLE READ**（default）
  
     可重复读就是一个事务只能读到另一个事务修改的已提交了事务的数据，但是第一次读取的数据，即使别的事务修改的这个值，这个事务再读取这条数据的时候还是和第一次获取的一样，不会随着别的事务的修改而改变。这和已提交读的区别就在于，它重复读取的值是不变的。所以取了个贴切的名字叫可重复读。按照这个隔离级别下那上面的例子就是：
  
     ![img](https://pics7.baidu.com/feed/b8014a90f603738d0046c82566532755f819ec73.jpeg?token=3468af6b225f722e94278088d03bb399&s=939E5522C659D02148D411CA020010BA)
  
  3. **READ COMMITTED**
  
     按照上面那个例子，在已提交读的情况下，事务A的select name 的结果是小刚，而不是小明，因为在这个隔离级别下，一个事务只能读到另一个事务修改的已经提交了事务的数据。但是有个现象，还是拿上面的例子说。如果事务B 在这时候隐式提交了时候，然后事务A的select name结果就是小明了，这都没问题，但是事务A还没结束，这时候事务B又`update table set name = '小红' where id = 1`并且隐式提交了。然后事务A又执行了一次`select name from table where id = 1`结果就返回了小红。这种现象叫不可重复读。
  
     ![img](https://pics2.baidu.com/feed/b8014a90f603738d06e6d24966532755f919ecff.jpeg?token=f3942f0dbe1c89ac9651c8a4e25cccde&s=D39E7422C218CC2144D511DE020010BA)
  
  4. **READ UNCOMMITTED**
  
     未提交读的意思就是比如原先name的值是小刚，然后有一个事务B`update table set name = '小明' where id = 1`,它还没提交事务。同时事务A也起了，有一个select语句`select name from table where id = 1`，在这个隔离级别下获取到的name的值是小明而不是小刚。那万一事务B回滚了，实际数据库中的名字还是小刚，事务A却返回了一个小明，这就称之为脏读。
  
     ![img](https://pics0.baidu.com/feed/caef76094b36acaf2dfda493a9911a1401e99c08.jpeg?token=f24408dc594e6d53e4e7dfaedda71b6b&s=D39E532AE658C0290661B8D6020010BA)
- The low-level details of `InnoDB` [locking](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_locking). During performance tuning, you see these details through `INFORMATION_SCHEMA` tables.

### Durability

指一个事务一旦提交，它对数据库中的数据的改变就应该是永久性的。接下来的其它操作或故障不应该对其执行结果有任何影响。

*Mysql怎么保证持久性的？*

 　　利用Innodb的`redo log（`重做日志）。

　　 正如之前说的，Mysql是先把磁盘上的数据加载到内存中，在内存中对数据进行修改，再刷回磁盘上。如果此时突然宕机，内存中的数据就会丢失。

　　 *怎么解决这个问题？* 

　　简单啊，事务提交前直接把数据写入磁盘就行啊。 

　　*这么做有什么问题？*

- 只修改一个页面里的一个字节，就要将整个页面刷入磁盘，太浪费资源了。毕竟一个页面16kb大小，你只改其中一点点东西，就要将16kb的内容刷入磁盘，听着也不合理。
- 毕竟一个事务里的SQL可能牵涉到多个数据页的修改，而这些数据页可能不是相邻的，也就是属于随机IO。显然操作随机IO，速度会比较慢。

　　采用`redo log`解决上面的问题。当做数据修改的时候，不仅在内存中操作，还会在`redo log`中记录这次操作。当事务提交的时候，会将`redo log`日志进行刷盘(`redo log`一部分在内存中，一部分在磁盘上)。当数据库宕机重启的时候，会将`redo log`中的内容恢复到数据库中，再根据`undo log`和`binlog`内容决定回滚数据还是提交数据。











