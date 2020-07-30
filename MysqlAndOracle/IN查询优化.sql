-- ======================mysql=====================================
DROP TABLE IF EXISTS `all_apple`;
CREATE TABLE `all_apple`  (
  `OID` varchar(50) ,
  `NAME` varchar(100),
  PRIMARY KEY (`OID`)
);

DROP TABLE IF EXISTS `bad_apple`;
CREATE TABLE `bad_apple`  (
  `OID` varchar(50) ,
  `NAME` varchar(100),
  PRIMARY KEY (`OID`)
);
-- ======================oracle=====================================
CREATE TABLE all_apple  (
  OID varchar2(50) ,
  NAME varchar2(100),
  PRIMARY KEY (OID)
);

CREATE TABLE bad_apple  (
  OID varchar2(50) ,
  NAME varchar2(100),
  PRIMARY KEY (OID)
);



--delete/update join : mysql支持，oracle不支持
DELETE aa FROM all_apple aa JOIN bad_apple ba ON ba.OID = aa.OID;
UPDATE all_apple aa JOIN bad_apple ba ON ba.OID = aa.OID SET aa.`NAME`='' WHERE aa.`NAME`='';

-- oracle不支持delete/update join，可以使用exists替换
DELETE FROM all_apple aa WHERE EXISTS (SELECT 1 FROM bad_apple ba WHERE ba.OID = aa.OID);
UPDATE all_apple aa SET aa.NAME = '' WHERE EXISTS (SELECT 1 FROM bad_apple ba WHERE ba.OID = aa.OID);

-- select join : mysql和oracle都支持
SELECT aa.* FROM all_apple aa JOIN bad_apple ba ON ba.OID = aa.OID;
