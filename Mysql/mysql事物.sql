BEGIN;
INSERT INTO img_table(img_name,img_data) VALUES('img01',null);
#当前窗口可以查到新增记录，但是在其他窗口查询不到新增记录，事物还没有提交
SELECT * FROM img_table;
rollback;
#当前窗口查不到新增记录，因为事物已经回滚
SELECT * FROM img_table;
#=================================================================
BEGIN;
INSERT INTO img_table(img_name,img_data) VALUES('img01',null);
SAVEPOINT a;
INSERT INTO img_table(img_name,img_data) VALUES('img02',null);
ROLLBACK to a;
#执行DDL语句会隐式提交事物
ALTER TABLE img_table MODIFY COLUMN img_name VARCHAR(10);
#提交事物
COMMIT;
