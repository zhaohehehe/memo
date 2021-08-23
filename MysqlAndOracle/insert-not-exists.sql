-- ======================mysql=====================================
insert ignore into ...

-- ======================oracle=====================================
DROP TABLE lock_test_a;
CREATE TABLE lock_test_a (
  id varchar2(255) NOT NULL,
  u_1 varchar2(255) NOT NULL,
  u_2 varchar2(255) NOT NULL,
	i_3 varchar2(255) DEFAULT NULL,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX unique_key_a ON lock_test_a(u_1,u_2);

DROP TABLE lock_test_b;
CREATE TABLE lock_test_b (
  id varchar2(255) NOT NULL,
  u_1 varchar2(255) NOT NULL,
  u_2 varchar2(255) NOT NULL,
	i_3 varchar2(255) DEFAULT NULL,
  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX unique_key_b ON lock_test_b(u_1,u_2);

DELETE FROM lock_test_a;
SELECT * FROM lock_test_a;


MERGE INTO lock_test_a A USING (select '001' u_1, '001' u_2 from dual) B ON(A.u_1 = B.u_1 and A.u_2 = B.u_2)
WHEN MATCHED THEN
	UPDATE SET i_3 = '00000'
WHEN NOT MATCHED THEN
	INSERT(id, u_1, u_2, i_3) VALUES ('001', B.u_1, B.u_2, '001');

