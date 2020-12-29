-- oracle rownum>=1
select rownum,t.* from tableName t;

-- mysql 变量
SELECT @rownum:=0;
select @rownum:=@rownum+1 AS rownum,  T.* from tableName T,(SELECT @rownum:=0) rowT;

-- mysql 统计增量
CREATE TABLE FLOWER(
	DATE VARCHAR(10),
	NUM INTEGER
);
INSERT INTO FLOWER(DATE,NUM) VALUES ('101',10),('102',20),('103',10),('104',30);
SELECT * FROM FLOWER;

-- 统计每天的小红花总数
SELECT a.DATE,SUM(b.NUM) FROM FLOWER a JOIN FLOWER b ON a.DATE >= b.DATE GROUP BY a.DATE;

-- 统计每天的小红花增率
SELECT @rownum:=@rownum+1 AS rownum,T.* FROM (SELECT a.DATE,SUM(b.NUM) sum FROM FLOWER a JOIN FLOWER b ON a.DATE >= b.DATE GROUP BY a.DATE) T,(SELECT @rownum:=0) rowT;

SELECT t1.*, t2.*, (t1.sum-t2.sum)/t1.sum increase_rate
FROM 
(SELECT @rownum:=@rownum+1 AS rownum,T.* FROM (SELECT a.DATE,SUM(b.NUM) sum FROM FLOWER a JOIN FLOWER b ON a.DATE >= b.DATE GROUP BY a.DATE) T,(SELECT @rownum:=0) rowT) t1 
INNER JOIN 
(SELECT @rownum2:=@rownum2+1 AS rownum,T.* FROM (SELECT a.DATE,SUM(b.NUM) sum FROM FLOWER a JOIN FLOWER b ON a.DATE >= b.DATE GROUP BY a.DATE) T,(SELECT @rownum2:=0) rowT) t2 
ON t1.rownum - 1 = t2.rownum;
