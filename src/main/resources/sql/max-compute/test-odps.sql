DROP TABLE IF EXISTS `testodpsdrivertable`;
DROP TABLE IF EXISTS `test_odps`;
CREATE TABLE `test_odps`
(
    `key`   INT,
    `value` STRING
);
INSERT INTO `test_odps`
    (`key`, `value`)
VALUES (1, 'Hello world!'),
       (2, 'weixin'),
       (3, 'dawn')
