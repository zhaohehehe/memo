## 准备表结构
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
INSERT INTO `lock_test`(`id`, `u1_1`, `u1_2`, `idx1_1`, `idx1_2`, `idx2`, `username`, `pwd`) VALUES ('3', 'u1_1', 'u1_3', 'idx1_1', 'idx1_2', 'idx2', 'panda', '111');
INSERT INTO `lock_test`(`id`, `u1_1`, `u1_2`, `idx1_1`, `idx1_2`, `idx2`, `username`, `pwd`) VALUES ('4', 'u1_1', 'u1_4', 'idx1_1', 'idx1_3', 'idx2', 'monkey', '222');
INSERT INTO `lock_test`(`id`, `u1_1`, `u1_2`, `idx1_1`, `idx1_2`, `idx2`, `username`, `pwd`) VALUES ('5', 'u1_1', 'u1_5', 'idx1_1', 'idx1_3', 'idx2', 'monkey', '222');
INSERT INTO `lock_test`(`id`, `u1_1`, `u1_2`, `idx1_1`, `idx1_2`, `idx2`, `username`, `pwd`) VALUES ('6', 'u1_1', 'u1_6', 'idx1_1', 'idx1_3', 'idx2', 'monkey', '222');

```
## 查看并设置隔离级别为RC
```
select @@global.transaction_isolation, @@session.transaction_isolation, @@transaction_isolation;
set session transaction isolation level read committed;
set global transaction isolation level read committed;
```

## DELETE
1. delete存在的主键：
- `begin;delete from lock_test where id = '1';`
- 查看lock:【8】`select index_name,'|' c1,lock_type,'|' c2,lock_mode,'|' c3,lock_status,'|' c4,lock_data from performance_schema.data_locks;`

```
index_name	c1	lock_type	c2	lock_mode	c3	lock_status	c4	lock_data
	|	TABLE	|	IX	|	GRANTED	|
PRIMARY	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'1'
```

2. delete存在的唯一索引：
- `begin;delete from lock_test where u1_1 = 'u1_1';`
- 查看lock:【8】`select index_name,'|' c1,lock_type,'|' c2,lock_mode,'|' c3,lock_status,'|' c4,lock_data from performance_schema.data_locks;`

```
index_name	c1	lock_type	c2	lock_mode	c3	lock_status	c4	lock_data
	|	TABLE	|	IX	|	GRANTED	|
u1	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'u1_1', 'u1_1', '1'
u1	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'u1_1', 'u1_3', '3'
u1	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'u1_1', 'u1_5', '5'
u1	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'u1_1', 'u1_6', '6'
u1	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'u1_1', 'u1_2', '2'
u1	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'u1_1', 'u1_4', '4'
PRIMARY	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'1'
PRIMARY	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'2'
PRIMARY	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'3'
PRIMARY	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'4'
PRIMARY	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'5'
PRIMARY	|	RECORD	|	X,REC_NOT_GAP	|	GRANTED	|	'6'
```

3. 如果delete记录不存在，只添加`|	TABLE	|	IX`锁。
4. 练习
