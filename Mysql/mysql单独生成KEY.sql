DROP TABLE IF EXISTS `beaf_service_roles_relation2`;
CREATE TABLE `beaf_service_roles_relation2` (
  `OID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ROLES` varchar(200) NOT NULL,
  `BEAF_SERVICE` varchar(200) NOT NULL,
  `CREATE_DATE` datetime DEFAULT NULL,
  `DELETE_FLAG` bigint(20) DEFAULT NULL,
  `RUN_STATUS` bigint(20) DEFAULT NULL,
	KEY(OID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

