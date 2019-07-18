- ### 15.7.1 InnoDB Locking

  - [Shared and Exclusive Locks](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html#innodb-shared-exclusive-locks)
  - [Intention Locks](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html#innodb-intention-locks)
  
  - [Record Locks](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html#innodb-record-locks)
  - [Gap Locks](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html#innodb-gap-locks)
  
  - [Next-Key Locks](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html#innodb-next-key-locks)
  - [Insert Intention Locks](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html#innodb-insert-intention-locks)
  - [AUTO-INC Locks](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html#innodb-auto-inc-locks)
  - [Predicate Locks for Spatial Indexes](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html#innodb-predicate-locks)
  
  #### Shared and Exclusive Locks（<span style="color:red">*row-level locking*</span>）

  - A [shared (`S`) lock](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_shared_lock) permits the transaction that holds the lock to **read a row.**
  - An [exclusive (`X`) lock](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_exclusive_lock) permits the transaction that holds the lock to **update or delete a row.**
  - 添加X锁： [`LOCK TABLES ... WRITE`](https://dev.mysql.com/doc/refman/8.0/en/lock-tables.html) 

  > If transaction `T1` holds a shared (`S`) lock on row `r`, then requests from some distinct transaction `T2` for a lock on row `r` are handled as follows:

  - A request by `T2` for an `S` lock can be granted immediately. As a result, both `T1` and `T2` hold an `S` lock on `r`.
  - A request by `T2` for an `X` lock cannot be granted immediately.
  
  > If a transaction `T1` holds an exclusive (`X`) lock on row `r`:
  
  - A request from some distinct transaction `T2` for a lock of either type on `r` cannot be granted immediately. Instead, transaction `T2` has to wait for transaction `T1` to release its lock on row `r`.
  
  #### Intention Locks（<span style="color:red">*table-level locking*</span>）
  
  `InnoDB` 支持*multiple granularity locking* 多粒度锁，允许行级锁和表级锁同时存在。为了使多粒度起作用，使用意向锁 [intention locks](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_intention_lock).他表示某个事物即将需要什么类型的锁，S或者是X锁。
  
  - An [intention shared lock](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_intention_shared_lock) (`IS`) indicates that a transaction intends to set a *shared* lock on individual rows in a table.
  - An [intention exclusive lock](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_intention_exclusive_lock) (`IX`) indicates that a transaction intends to set an exclusive lock on individual rows in a table.
  - 设置IS 锁： [`SELECT ... FOR SHARE`](https://dev.mysql.com/doc/refman/8.0/en/select.html) 
  - 设置IX锁： [`SELECT ... FOR UPDATE`](https://dev.mysql.com/doc/refman/8.0/en/select.html) 
  
  The intention locking 遵循协议:
  
  - 事物获取某行的S锁之前，必须首先获取表上的IS锁或者更强的锁。
  
  - 事物获取某行的X锁之前，必须首先获取表上的IX锁。
  
  - 所以，意向锁不会block **full table requests**(for example, [`LOCK TABLES ... WRITE`](https://dev.mysql.com/doc/refman/8.0/en/lock-tables.html)). 请求以外的任何内容，他的主要目的是为了展示有人正在锁定一行或者即将锁定一行。
  
  - 事物数据：
  
    ```sql
    TABLE LOCK table `test`.`t` trx id 10080 lock mode IX
    ```
  
  Table-level lock type compatibility is summarized in the following matrix.
  
  |      | `X`      | `IX`       | `S`        | `IS`       |
  | ---- | -------- | ---------- | ---------- | ---------- |
  | `X`  | Conflict | Conflict   | Conflict   | Conflict   |
  | `IX` | Conflict | Compatible | Conflict   | Compatible |
  | `S`  | Conflict | Conflict   | Compatible | Compatible |
  | `IS` | Conflict | Compatible | Compatible | Compatible |
  
  #### Record Locks（<span style="color:red">*a lock on an index record*</span>）
  
  -  `SELECT c1 FROM t WHERE c1 = 10 FOR UPDATE;` prevents any other transaction from inserting, updating, or deleting rows where the value of `t.c1` is `10`.
  - 总是锁定索引记录，如果没有定义，InnoDB会自动创建a hidden clustered index。
  - 事物数据：
  
  ```sql
  RECORD LOCKS space id 58 page no 3 n bits 72 index `PRIMARY` of table `test`.`t` 
  trx id 10078 lock_mode X locks rec but not gap
  Record lock, heap no 2 PHYSICAL RECORD: n_fields 3; compact format; info bits 0
   0: len 4; hex 8000000a; asc     ;;
   1: len 6; hex 00000000274f; asc     'O;;
   2: len 7; hex b60000019d0110; asc        ;;
  ```
  
  #### Gap Locks（<span style="color:red"> a gap between index records, or a lock on the gap before the first or after the last index record</span>）
  
  -  `SELECT c1 FROM t WHERE c1 BETWEEN 10 and 20 FOR UPDATE;` prevents other transactions from inserting a value of `15` into column `t.c1`, 不论表中是否存在这样的数据。
  
  - A gap might span a single index value, multiple index values, or even be empty.
  
  - Gap locks  are used in some transaction isolation levels and not others.
  
  - Gap locking不适合用一个unique index 检索一个unique row。 For example, if the `id` column has a unique index, the following statement uses only an index-record lock for the row having `id` value 100 and it does not matter whether other sessions insert rows in the preceding gap:
  
  ```sql
  SELECT * FROM child WHERE id = 100;
  ```
  
  不过，If `id` is not indexed or has a nonunique index, the statement does lock the preceding gap.
  
  - 这里也值得注意的是，冲突的锁可以通过不同的事务保持在间隙上。例如，事务A可以在间隙上持有共享间隙锁（GAP S-LOCK），而事务B可以在同一间隙上持有独占间隙锁（GAP X-LOCK）。允许冲突的间隙锁的原因是，如果从索引中清除某个记录，则必须合并不同事务对该记录保留的间隙锁。
  
  - GAP的唯一目的是**防止**其他事务插入间隙。间隙锁可以共存。一个事务获取的间隙锁不会阻止另一个事务在同一间隙上获取间隙锁。共享和独占的间隙锁没有区别。它们不会相互冲突，并且执行相同的功能。
  
  #### Next-Key Locks（<span style="color:red">*a combination of a record lock on the index record and a gap lock on the gap before the index record*</span>）
  
  - A next-key lock is a combination of **a record lock on the index record** and **a gap lock on the gap before the index record**.
  - 在聚集索引中，如果主键有唯一性约束（unique，auto increment），next-key locking 会自动降级为record locking。
  - 如果一个会话在索引中的记录“r”上具有共享或独占锁定，则另一个会话无法在索引顺序中紧接“r”之前的间隙中插入新的索引记录。
  - 假设索引包含值10、11、13和20。此索引可能的 next-key lock 包括以下间隔，其中圆括号表示不包括间隔端点，方括号表示包含端点：
  
  ```none
  (negative infinity, 10]
  (10, 11]
  (11, 13]
  (13, 20]
  (20, positive infinity)
  ```
  
  对于最后一个间隔 next-key lock 会将间隙锁定在索引中最大值的上方，“supremum”伪记录的值高于索引中实际的任何值。supremum不是一个真正的索引记录，因此，实际上，这个 next-key lock 只锁定最大索引值之后的间隙。
  
  - 事物数据:
  
  ```sql
  RECORD LOCKS space id 58 page no 3 n bits 72 index `PRIMARY` of table `test`.`t` 
  trx id 10080 lock_mode X
  Record lock, heap no 1 PHYSICAL RECORD: n_fields 1; compact format; info bits 0
   0: len 8; hex 73757072656d756d; asc supremum;;
  
  Record lock, heap no 2 PHYSICAL RECORD: n_fields 3; compact format; info bits 0
   0: len 4; hex 8000000a; asc     ;;
   1: len 6; hex 00000000274f; asc     'O;;
   2: len 7; hex b60000019d0110; asc        ;;
  ```
  
  #### Insert Intention Locks
  
  插入意向锁是在行插入之前由 [`INSERT`](https://dev.mysql.com/doc/refman/8.0/en/insert.html) 操作设置的一种间隙锁。这个锁表示插入的意图，如果插入到同一索引间隙中的多个事务没有插入到间隙中的同一位置，那么它们不需要等待对方。假设有值为4和7的索引记录。尝试分别插入值5和6的单独事务，在获取插入行的独占锁之前，每个事务都用插入意图锁锁定4和7之间的间隙，但不会相互阻塞，因为行不冲突。
  
  The following example demonstrates a transaction taking an insert intention lock prior to obtaining an exclusive lock on the inserted record. The example involves two clients, A and B.
  
  Client A creates a table containing two index records (90 and 102) and then starts a transaction that places an exclusive lock on index records with an ID greater than 100. The exclusive lock includes a gap lock before record 102:
  
  ```sql
  mysql> CREATE TABLE child (id int(11) NOT NULL, PRIMARY KEY(id)) ENGINE=InnoDB;
  mysql> INSERT INTO child (id) values (90),(102);
  
  mysql> START TRANSACTION;
  mysql> SELECT * FROM child WHERE id > 100 FOR UPDATE;
  +-----+
  | id  |
  +-----+
  | 102 |
  +-----+
  ```
  
  Client B begins a transaction to insert a record into the gap. The transaction takes an insert intention lock while it waits to obtain an exclusive lock.
  
  ```sql
  mysql> START TRANSACTION;
  mysql> INSERT INTO child (id) VALUES (101);
  ```
  
  事物数据:
  
  ```sql
  RECORD LOCKS space id 31 page no 3 n bits 72 index `PRIMARY` of table `test`.`child`
  trx id 8731 lock_mode X locks gap before rec insert intention waiting
  Record lock, heap no 3 PHYSICAL RECORD: n_fields 3; compact format; info bits 0
   0: len 4; hex 80000066; asc    f;;
   1: len 6; hex 000000002215; asc     " ;;
   2: len 7; hex 9000000172011c; asc     r  ;;...
  ```
  
  #### AUTO-INC Locks
  
  #### Predicate Locks for Spatial Indexes
  
  ### 行锁的兼容矩阵
  
  |                  | Gap（持有） | Insert Intention | Record | Next-Key |
  | :--------------: | :---------: | :--------------: | :----: | :------: |
  |   Gap（请求）    |    兼容     |       兼容       |  兼容  |   兼容   |
  | Insert Intention |   `冲突`    |       兼容       |  兼容  |  `冲突`  |
  |      Record      |    兼容     |       兼容       | `冲突` |  `冲突`  |
  |     Next-Key     |    兼容     |       兼容       | `冲突` |  `冲突`  |
  
  表注：横向是已经持有的锁，纵向是正在请求的锁。
  
### 共享锁、排他锁与意向锁的兼容矩阵如下：
  
|      |   X    |   IX   |   S    |   IS   |
  | :--: | :----: | :----: | :----: | :----: |
  |  X   | `冲突` | `冲突` | `冲突` | `冲突` |
  |  IX  | `冲突` |  兼容  | `冲突` |  兼容  |
  |  S   | `冲突` | `冲突` |  兼容  |  兼容  |
  |  IS  | `冲突` |  兼容  |  兼容  |  兼容  |