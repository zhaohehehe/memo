-- oracle index>=1
select * from 
(
	select table_name.*,ROWNUM as rowno from table_name where ROWNUM <=20
) table_alias where table_alias.rowno>=10;

select * from                       
     ( select target.*,ROWNUM rn from (select * from emp) target where ROWNUM<=currentPage*size ) 
where rn>(currentPage-1)*size;

-- mysql index>=1
-- 前1条
select * from city limit 1;
-- 前2条
select * from city limit 2;
-- offset=1的1条
select * from city limit 1,1;
-- offset=2的1条
select * from city limit 2,1;
-- offset=2的10条
select * from city limit 2,10;

select * from emp limit (currentPage-1)*size,size
 