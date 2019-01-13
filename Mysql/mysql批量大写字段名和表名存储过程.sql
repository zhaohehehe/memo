--MYSQL批量大写字段名-------------------------------------------------------------
CREATE DEFINER=`sjzc_yf`@`%` PROCEDURE `BATCH_UPPER_COL_UTIL`(IN dbname VARCHAR(200))
BEGIN  
   
DECLARE done_tb INT DEFAULT 0;   
   
DECLARE oldname_tb VARCHAR(200);   
   
DECLARE cur_tb CURSOR FOR SELECT table_name FROM information_schema.TABLES WHERE table_schema = dbname and TABLE_TYPE='BASE TABLE';   
   
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done_tb = 1;   
   
OPEN cur_tb;   
   
REPEAT   
   
FETCH cur_tb INTO oldname_tb;   
   
    IF done_tb!=1 THEN  
        call UPPER_COL_UTIL(oldname_tb,dbname);
    END IF;    
UNTIL done_tb END REPEAT;   
   
CLOSE cur_tb; 
   
END
--MYSQL大写字段名----------------------------------------------------------------
CREATE DEFINER=`sjzc_yf`@`%` PROCEDURE `UPPER_COL_UTIL`(IN tbname VARCHAR(200),IN tbschema VARCHAR(200))
BEGIN  
   
DECLARE done_col INT DEFAULT 0;   
   
DECLARE oldname_col VARCHAR(200);   

DECLARE oldtype_col VARCHAR(200); 
   
DECLARE cur_col CURSOR FOR SELECT COLUMN_NAME,COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_NAME=	tbname and TABLE_SCHEMA = tbschema;   
   
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done_col = 1;   
   
OPEN cur_col;   
   
REPEAT   
   
FETCH cur_col INTO oldname_col,oldtype_col; 
  
SET @newname_col =  upper(oldname_col);   
     
SET @SQL = CONCAT('alter table ',tbname,' change ',oldname_col,' ',@newname_col,' ',oldtype_col);    
   
PREPARE tmpstmt_col FROM @SQL;   
   
EXECUTE tmpstmt_col;   
   
DEALLOCATE PREPARE tmpstmt_col;   
   
UNTIL done_col END REPEAT;   
   
CLOSE cur_col;   
   
END
--MYSQL大写表名-------------------------------------------------------------
CREATE DEFINER=`sjzc_yf`@`%` PROCEDURE `UPPER_TABLE_UTIL`(IN dbname VARCHAR(200))
BEGIN  
   
DECLARE done_tb INT DEFAULT 0;   
   
DECLARE oldname_tb VARCHAR(200);   
   
DECLARE cur_tb CURSOR FOR SELECT table_name FROM information_schema.TABLES WHERE table_schema = dbname and TABLE_TYPE='BASE TABLE';   
   
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done_tb = 1;   
   
OPEN cur_tb;   
   
REPEAT   
   
FETCH cur_tb INTO oldname_tb;   
   
SET @newname_tb =  upper(oldname_tb);   
     
SET @SQL = CONCAT('alter table ',oldname_tb,' rename to ',@newname_tb);   
   
PREPARE tmpstmt_tb FROM @SQL;   
   
EXECUTE tmpstmt_tb;   
   
DEALLOCATE PREPARE tmpstmt_tb;   
   
UNTIL done_tb END REPEAT;   
   
CLOSE cur_tb;   
   
END
--MYSQL顺序生成编码-------------------------------------------------------
CREATE DEFINER=`sjzc_yf`@`%` FUNCTION `GET_ASSET_CODE_SEQUENCE`(tenantId VARCHAR(50),
ruleField   VARCHAR(50)) RETURNS varchar(8) CHARSET utf8
begin
declare assetCodeNum  BIGINT default 0;
  select CODE_NUM into assetCodeNum from bdam_asset_code_sequence where TENANT_ID=tenantId and RULE_FIELD=ruleField;
  if assetCodeNum=0 
   then 
     SET assetCodeNum=assetCodeNum+1;
     INSERT into bdam_asset_code_sequence(OID,TENANT_ID,RULE_FIELD,CODE_NUM ) VALUES(UUID(),tenantId,ruleField,assetCodeNum+1);
   else 
      UPDATE bdam_asset_code_sequence SET CODE_NUM=(assetCodeNum+1)  where TENANT_ID=tenantId and RULE_FIELD=ruleField;
   end if;
	RETURN LPAD(assetCodeNum, 8, 0);
end
