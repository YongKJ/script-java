DROP TABLE IF EXISTS `ods_test_odps`;
CREATE TABLE `ods_test_odps`
(
    `key`   INT    NOT NULL DEFAULT 0,
    `value` STRING NOT NULL DEFAULT ''
) tblproperties("transactional"="true");
INSERT INTO `ods_test_odps`
    (`key`, `value`)
VALUES (1, 'Hello world!'),
       (2, 'weixin'),
       (3, 'dawn')
