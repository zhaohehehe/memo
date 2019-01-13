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
