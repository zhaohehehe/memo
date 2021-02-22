## 统计统计每个年级的前三名。
  1. Oracle样例数据如下。

  ```
  DROP TABLE TOP;
  CREATE TABLE TOP(S_NO VARCHAR2(50),S_NAME VARCHAR2(10),CLASS VARCHAR2(10),GRADE NUMBER);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'李一','一年级',94);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'张二','一年级',80);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'王三','一年级',70);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'王四','一年级',93);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'张五','一年级',70);

  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'李一','二年级',90);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'李二','二年级',80);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'张三','二年级',74);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'李四','二年级',90);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'张五','二年级',70);

  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'张一','三年级',98);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'王二','三年级',85);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'张三','三年级',73);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'王四','三年级',90);
  INSERT INTO TOP(S_NO,S_NAME,CLASS,GRADE) VALUES(sys_guid(),'张五','三年级',70);
  select * from TOP;
  ```
  2. Mysql样例数据(VARCHAR2换成VARCHAR，sys_guid换成uuid，NUMBER换成int)。

## Oracle
  Oracle支持分析函数：row_number() over(partition by column1 order by column1 desc)。
  ```
  --group by + order by ？？？？
  select a.*,b.* from
  (select class from top group by class) a
  join
  (select * from top order by grade desc) b
  on a.class = b.class
  order by a.class;
  -- row_number() over(partition by column1 order by column1 desc)
  select * from
  (select t.*,row_number() over(partition by class order by grade desc) sn from top t) tmp
  where tmp.sn <= 3
  ```
## Mysql
  Mysql不支持row_number() over分析函数，自实现。
  ```
  -- 获取行号
  select @rownum:=@rownum+1 as rownum,  T.* from top T,(SELECT @rownum:=0) top;
  -- 获取分组行号
  select T.*,if(@tmp = class, @rownum:=@rownum+1,@rownum:=1) as rownum,@tmp:=class from top T,(select @tmp:='' from dual) R;
  -- 前三名
  select * from (select T.*,if(@tmp = class, @rownum:=@rownum+1,@rownum:=1) as rownum,@tmp:=class from top T,(select @tmp:='' from dual) R order by T.class,T.grade desc) tmp where tmp.rownum <= 3
  ```
