-- ======================mysql=====================================
select unix_timestamp('2020-07-29 10:35:59') - unix_timestamp('2020-07-30 10:35:59') < 0;
-- unix——>月和日（省略月份前的0）
select from_unixtime(1596076559177/1000, '%c%d');




-- ======================oracle=====================================
-- timestamp——>date：
select to_date(to_char(systimestamp,'yyyy-mm-dd hh24:mi:ss'),'yyyy-mm-dd hh24:mi:ss') from dual;
-- date ——>timestamp：
select to_timestamp(to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),'yyyy-mm-dd hh24:mi:ss') from dual;
-- timestamp(n),n=1-9
select 
to_char(systimestamp, 'yyyy-mm-dd hh24:mi:ss:ff1') t_1,
to_char(systimestamp, 'yyyy-mm-dd hh24:mi:ss:ff3') t_3,
to_char(systimestamp, 'yyyy-mm-dd hh24:mi:ss:ff6') t_6,
to_char(systimestamp, 'yyyy-mm-dd hh24:mi:ss:ff9') t_9
from dual;
-- why
select 1 from dual where sysdate < systimestamp + 0;
select 1 from dual where sysdate < systimestamp;
select 1 from dual where to_date('2020-07-29 16:06:57','yyyy-mm-dd hh24:mi:ss') < systimestamp + 0;
select 1 from dual where to_date('2020-07-29 16:06:57','yyyy-mm-dd hh24:mi:ss') < systimestamp;
-- unix——>月和日（省略月份前的0）
select TO_CHAR(1596076559177 / (1000 * 60 * 60 * 24) + TO_DATE('19700101', 'yyyymmdd'), 'fmmmfmdd') from dual;
-- unix——>date 
-- 24h * 3600s/h = 86400 
-- TO_NUMBER(SUBSTR(TZ_OFFSET(sessiontimezone),1,3))单位为小时
SELECT TO_DATE('19700101', 'yyyymmdd')+ 1596076559177/1000/86400 + TO_NUMBER(SUBSTR(TZ_OFFSET(sessiontimezone),1,3))/24 FROM dual;
-- dateStr——>unix 
SELECT (TO_DATE('2020-07-30 10:35:59', 'yyyy-mm-dd hh24:mi:ss') - TO_DATE('19700101', 'yyyymmdd'))*86400 - TO_NUMBER(SUBSTR(TZ_OFFSET(sessiontimezone),1,3))*3600 FROM dual;
		-- 创建unix——>date函数
		CREATE OR REPLACE function unix_to_date(in_unix_number IN NUMBER)
		return DATE 
		IS
		BEGIN
			return TO_DATE('19700101', 'yyyymmdd')+ in_unix_number/86400 + TO_NUMBER(SUBSTR(TZ_OFFSET(sessiontimezone),1,3))/24;
		END unix_to_date;
		-- 调用方式
		SELECT unix_to_date(86400) from dual;


