DROP TABLE IF EXISTS `ods_little_advertising_report`;
CREATE TABLE `ods_little_advertising_report`
(
    `id`                BIGINT         NOT NULL DEFAULT 0,
    `adgroup_id`        BIGINT         NOT NULL DEFAULT 0,
    `adgroup_name`      STRING         NOT NULL DEFAULT '',
    `fee`               DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `impression`        INT            NOT NULL DEFAULT 0,
    `cpm`               DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `impression_cost`   DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `click`             INT            NOT NULL DEFAULT 0,
    `acp`               DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `click_cost`        DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `app_register_cost` DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `date`              BIGINT         NOT NULL DEFAULT 0,
    `utc_created`       BIGINT         NOT NULL DEFAULT 0,
    `utc_modified`      BIGINT         NOT NULL DEFAULT 0,
    `utc_deleted`       BIGINT         NOT NULL DEFAULT 0
) tblproperties("transactional"="true");
DROP TABLE IF EXISTS `ods_little_advertising`;
CREATE TABLE `ods_little_advertising`
(
    `id`             BIGINT NOT NULL DEFAULT 0,
    `promotion_id`   BIGINT NOT NULL DEFAULT 0,
    `promotion_name` STRING NOT NULL DEFAULT '',
    `promotion_type` INT    NOT NULL DEFAULT 0,
    `utc_created`    BIGINT NOT NULL DEFAULT 0,
    `utc_modified`   BIGINT NOT NULL DEFAULT 0,
    `utc_deleted`    BIGINT NOT NULL DEFAULT 0
) tblproperties("transactional"="true")
