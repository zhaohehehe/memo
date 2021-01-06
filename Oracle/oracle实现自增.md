## 准备
```
CREATE TABLE TEST_SEMANTIC_INCREMENT
(
	ID VARCHAR2(400),
	DGS_TYSHXYDM VARCHAR2(400),
	ZCZB2 NUMBER(24,4),
	HZRQ DATE,  
	JYQX DATE,
	XYDMBM VARCHAR2(400),
	IS_VALID VARCHAR2(1),
	AUDIT_STATUS VARCHAR2(50),
	SYS_ENCRYPT CLOB
);
```
```
INSERT INTO TEST_SEMANTIC_INCREMENT(DGS_TYSHXYDM, ZCZB2, HZRQ, JYQX, XYDMBM, IS_VALID, AUDIT_STATUS, SYS_ENCRYPT) VALUES ('1', '1', sysdate, sysdate, '1', '1', '1', '1');
```
## 方式1: sequence
1. 创建sequence：同上
2. 显式调用
```
select seq_test_semantic.currval from dual;
select seq_test_semantic.nextval from dual;
```
3. 查看效果
```
INSERT INTO TEST_SEMANTIC_INCREMENT(ID, DGS_TYSHXYDM, ZCZB2, HZRQ, JYQX, XYDMBM, IS_VALID, AUDIT_STATUS, SYS_ENCRYPT) VALUES (seq_test_semantic.nextval, '1', '1', sysdate, sysdate, '1', '1', '1', '1');
select * from TEST_SEMANTIC_INCREMENT;
```

## 方式2: sequence + trigger
1. 创建sequence
```
create sequence seq_test_semantic
increment by 1
start with 1
minvalue 1
maxvalue 999999
cache 50;
```
```
drop sequence seq_test_semantic;
```
2. 创建trigger
```
create or replace trigger trigger_test_semantic
    before insert on TEST_SEMANTIC_INCREMENT
    referencing old as old new as new for each row
declare
begin
    select seq_test_semantic.nextval into :new.ID from dual;
end trigger_test_semantic;
```
```
drop trigger trigger_test_semantic;
```
3. 查看效果
```
INSERT INTO TEST_SEMANTIC_INCREMENT(DGS_TYSHXYDM, ZCZB2, HZRQ, JYQX, XYDMBM, IS_VALID, AUDIT_STATUS, SYS_ENCRYPT) VALUES ('1', '1', sysdate, sysdate, '1', '1', '1', '1');
select * from TEST_SEMANTIC_INCREMENT;
```
