
-- mysql
DROP TABLE IF EXISTS `BLACK_LIST`;
CREATE TABLE `BLACK_LIST` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dimension` varchar(32) NOT NULL DEFAULT '' COMMENT '维度',
  `type` varchar(16) NOT NULL DEFAULT '' COMMENT '类型',
  `value` varchar(128) NOT NULL DEFAULT '' COMMENT '值',
  `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间',
  `detail` varchar(512) DEFAULT NULL COMMENT '详情',
  PRIMARY KEY (`id`),
  KEY (`dimension`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `CONFIG`;
CREATE TABLE `CONFIG` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key` varchar(32) NOT NULL DEFAULT ''  COMMENT '键',
  `value` varchar(128) NOT NULL DEFAULT '' COMMENT '值',
  `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间',
  `detail` varchar(512) NOT NULL DEFAULT '' COMMENT '详情',
  PRIMARY KEY (`id`),
  UNIQUE KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

  -- ----------------------------
  -- Records of CONFIG
  -- ----------------------------
INSERT INTO `CONFIG`(`key`,`value`,detail) VALUES ('SWITCH_RC', 'ON', '风控分析开关，ON开启分析，OFF关闭分析消息丢弃');

