-- oracle
ALTER TABLE ${tablename} ADD (id VARCHAR2(100),name VARCHAR2(10));
ALTER TABLE ${tablename} DROP (id,name);

-- mysql
ALTER TABLE ${tablename} ADD (id VARCHAR(100),name VARCHAR(10));
ALTER TABLE ${tablename} DROP COLUMN id,DROP COLUMN name;