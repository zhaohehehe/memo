## 1. 准备表结构
```
DROP TABLE lock_test;
CREATE TABLE lock_test (
    id VARCHAR ( 255 ) NOT NULL,
    u1_1 VARCHAR ( 255 ) DEFAULT NULL,
    u1_2 VARCHAR ( 255 ) DEFAULT NULL,
    idx1_1 VARCHAR ( 255 ) DEFAULT NULL,
    idx1_2 VARCHAR ( 255 ) DEFAULT NULL,
    idx2 VARCHAR ( 255 ) DEFAULT NULL,
    username VARCHAR ( 255 ) DEFAULT NULL,
    pwd VARCHAR ( 255 ) DEFAULT NULL,
    PRIMARY KEY ( id ),
    UNIQUE KEY u1 ( u1_1, u1_2 ) USING BTREE,
    KEY idx1 ( idx1_1, idx1_2 ) USING BTREE,
    KEY idx2 ( idx2 ) USING BTREE
) ENGINE = INNODB DEFAULT CHARSET = utf8;
INSERT INTO `lock_test`(`id`, `u1_1`, `u1_2`, `idx1_1`, `idx1_2`, `idx2`, `username`, `pwd`) VALUES ('1', 'u1_1', 'u1_1', 'idx1_1', 'idx1_2', 'idx2', 'panda', '111');
INSERT INTO `lock_test`(`id`, `u1_1`, `u1_2`, `idx1_1`, `idx1_2`, `idx2`, `username`, `pwd`) VALUES ('2', 'u1_1', 'u1_2', 'idx1_1', 'idx1_2', 'idx2', 'panda', '111');
INSERT INTO `lock_test`(`id`, `u1_1`, `u1_2`, `idx1_1`, `idx1_2`, `idx2`, `username`, `pwd`) VALUES ('6', 'u1_6', 'u1_6', 'idx1_1', 'idx1_3', 'idx2', 'monkey', '222');

或者

DROP TABLE lock_test_2;
CREATE TABLE `lock_test_2` (
  `id` varchar(255) NOT NULL,
  `u_1` varchar(255) DEFAULT NULL,
  `u_2` varchar(255) DEFAULT NULL,
  `key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key` (`u_1`,`u_2`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `lock_test_2`(`id`, `u_1`, `u_2`, `key`) VALUES ('001', '001', '001', '001');
INSERT INTO `lock_test_2`(`id`, `u_1`, `u_2`, `key`) VALUES ('002', '001', '002', '002');
INSERT INTO `lock_test_2`(`id`, `u_1`, `u_2`, `key`) VALUES ('005', '005', '005', '005');
INSERT INTO `lock_test_2`(`id`, `u_1`, `u_2`, `key`) VALUES ('009', '009', '009', '009');

```
## 2. 查看并设置隔离级别为RC
```
select @@global.transaction_isolation, @@session.transaction_isolation, @@transaction_isolation;
set session transaction isolation level read committed;
set global transaction isolation level read committed;
```
## 3. 按照以下事务顺序执行

##### T1：（insert成功）
```
begin;
INSERT INTO `lock_test`(`id`, `u1_1`, `u1_2`, `idx1_1`, `idx1_2`, `idx2`, `username`, `pwd`) VALUES ('3', 'u1_3', 'u1_3', 'idx1_1', 'idx1_2', 'idx2', 'panda', '111');
或者
insert into lock_test(id,u_1,u_2,`key`)  values('003','003','003','003');

92566		|	TABLE	|	IX	|	GRANTED	|
```

##### T2：（insert唯一索引冲突）
```
begin;
INSERT INTO `lock_test`(`id`, `u1_1`, `u1_2`, `idx1_1`, `idx1_2`, `idx2`, `username`, `pwd`) VALUES ('4', 'u1_3', 'u1_3', 'idx1_1', 'idx1_3', 'idx2', 'monkey', '222');
或者
insert into lock_test_2(id,u_1,u_2,`key`) values('004','003','003','004');

92567		|	TABLE	|	IX	|	GRANTED	|
92567	unique_key	|	RECORD	|	S	|	WAITING	|	'003', '003', '003'
92566		|	TABLE	|	IX	|	GRANTED	|
92566	unique_key	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'003', '003', '003'

```

##### T3：
```
begin;
【会出现GAP】INSERT INTO `lock_test`(`id`, `u1_1`, `u1_2`, `idx1_1`, `idx1_2`, `idx2`, `username`, `pwd`) VALUES ('5', 'u1_2', 'u1_2', 'idx1_1', 'idx1_3', 'idx2', 'monkey', '222');
或者
【会出现GAP】insert into lock_test_2(id,u_1,u_2,`key`) values('006','002','002','002');
【会出现GAP】insert into lock_test_2(id,u_1,u_2,`key`) values('006','002','003','002');
【会出现GAP】insert into lock_test_2(id,u_1,u_2,`key`) values('006','002','004','002');  
【会出现GAP】insert into lock_test_2(id,u_1,u_2,`key`) values('006','002','${任意值}','002');

92568		|	TABLE	|	IX	|	GRANTED	|
92568	unique_key	|	RECORD	|	X,GAP,INSERT_INTENTION	|	WAITING	|	'003', '003', '003'
92567		|	TABLE	|	IX	|	GRANTED	|
92567	unique_key	|	RECORD	|	S	|	WAITING	|	'003', '003', '003'
92566		|	TABLE	|	IX	|	GRANTED	|
92566	unique_key	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'003', '003', '003'

【不会出现GAP】insert into lock_test_2(id,u_1,u_2,`key`)  values('006','001','002','006');
【不会出现GAP】insert into lock_test_2(id,u_1,u_2,`key`)  values('006','003','003','006');
```

***以lock_test_2来看，说明`唯一索引`u_1,u_2的区间`(('001','002'),('003','003'))`（不包含边界）被加上GAP锁。T3中添加这个区间内的记录都会导致出现GAP锁。***


## 4. 如果按照以下事务顺序执行
##### insert数据：
```
insert into lock_test_2(id,u_1,u_2,`key`) values('003','003','003','003');
```

##### T1：（insert唯一索引冲突）
```
insert into lock_test_2(id,u_1,u_2,`key`) values('004','003','003','004');
```

##### T2：
```
insert into lock_test_2(id,u_1,u_2,`key`) values('006','002','002','002');
```

##### 查看加锁情况：【8】select engine_transaction_id,index_name,'|' c1,lock_type,'|' c2,lock_mode,'|' c3,lock_status,'|' c4,lock_data from performance_schema.data_locks;
```
T1:：
92579		|	TABLE	|	IX	|	GRANTED	|
92579	unique_key	|	RECORD	|	S	|	GRANTED	|	'003', '003', '003'

T2：
92580		|	TABLE	|	IX	|	GRANTED	|
92580	unique_key	|	RECORD	|	X,GAP,INSERT_INTENTION	|	WAITING	|	'003', '003', '003'
92579		|	TABLE	|	IX	|	GRANTED	|
92579	unique_key	|	RECORD	|	S	|	GRANTED	|	'003', '003', '003'
```

## 5. 结论
  所以，RC级别下，一旦出现唯一索引冲突，就会将冲突记录之前的所有空白区间锁定，所有有意向向该区间insert记录的事务都需要获取（X,GAP,INSERT_INTENTION）锁，直到获取成功或者锁超时。
