#### 创建表和存储过程
```
-- 表
drop table baskets;
drop table apples;
CREATE TABLE `baskets` (`lock_key` varchar(255) NOT NULL,PRIMARY KEY(lock_key)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE `apples` (`lock_key` varchar(255) NOT NULL,`apples` bigint DEFAULT 0,PRIMARY KEY(lock_key)) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 存储过程increment_apple
drop procedure increment_apple;
delimiter $$
CREATE PROCEDURE increment_apple(in in_lock_key varchar(255), in seconds int)
begin
	declare error integer default 0;
	declare isExist integer default 0;
	-- declare continue handler for sqlexception set error = 1;
	-- start transaction;
		insert into baskets(lock_key) values (in_lock_key);
		select 1 into isExist from apples where lock_key = in_lock_key;
		if isExist=1 then
			update apples set apples = apples + 1 where lock_key = in_lock_key;
		else
			insert into apples(lock_key,apples) values (in_lock_key, 1);
		end if;
		select sleep(seconds);
		delete from baskets where lock_key = in_lock_key;
	-- if error = 1 then
	-- 	rollback;
	-- else
	-- 	commit;
	-- end if;
	-- select error;
end$$

-- 存储过程decrement_apple
drop procedure decrement_apple;
delimiter $$
CREATE PROCEDURE decrement_apple(in in_lock_key varchar(255), in seconds int)
begin
	declare error integer default 0;
	declare isExist integer default 0;
	-- declare continue handler for sqlexception set error = 1;
	-- start transaction;
		insert into baskets(lock_key) values (in_lock_key);
		select 1 into isExist from apples where lock_key = in_lock_key;
		if isExist=1 then
			update apples set apples = apples - 1 where lock_key = in_lock_key;
		else
			insert into apples(lock_key,apples) values (in_lock_key, -1);
		end if;
		select sleep(seconds);
		delete from baskets where lock_key = in_lock_key;
	-- if error = 1 then
	-- 	rollback;
	-- else
	-- 	commit;
	-- end if;
	-- select error;
end$$

-- 存储过程sleep
delimiter $$
CREATE PROCEDURE `sleep`(in seconds int)
begin
	 select sleep(seconds);
end$$
```
