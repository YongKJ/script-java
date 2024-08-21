DROP TABLE IF EXISTS `ods_tencent_advertising_report`;
CREATE TABLE `ods_tencent_advertising_report`
(
    `id`                         BIGINT         NOT NULL DEFAULT 0,
    `adgroup_id`                 BIGINT         NOT NULL DEFAULT 0,
    `adgroup_name`               STRING         NOT NULL DEFAULT '',
    `ad_id`                      BIGINT         NOT NULL DEFAULT 0,
    `ad_name`                    STRING         NOT NULL DEFAULT '',
    `cost`                       DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `view_count`                 INT            NOT NULL DEFAULT 0,
    `thousand_display_price`     DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `view_cost`                  DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `valid_click_count`          INT            NOT NULL DEFAULT 0,
    `cpc`                        DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `valid_click_cost`           DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `effect_leads_purchase_cost` DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `date`                       BIGINT         NOT NULL DEFAULT 0,
    `utc_created`                BIGINT         NOT NULL DEFAULT 0,
    `utc_modified`               BIGINT         NOT NULL DEFAULT 0,
    `utc_deleted`                BIGINT         NOT NULL DEFAULT 0
) tblproperties("transactional"="true");
DROP TABLE IF EXISTS `ods_tencent_advertising`;
CREATE TABLE `ods_tencent_advertising`
(
    `id`           BIGINT NOT NULL DEFAULT 0,
    `adgroup_id`   BIGINT NOT NULL DEFAULT 0,
    `adgroup_name` STRING NOT NULL DEFAULT '',
    `ad_id`        BIGINT NOT NULL DEFAULT 0,
    `ad_name`      STRING NOT NULL DEFAULT '',
    `utc_created`  BIGINT NOT NULL DEFAULT 0,
    `utc_modified` BIGINT NOT NULL DEFAULT 0,
    `utc_deleted`  BIGINT NOT NULL DEFAULT 0
) tblproperties("transactional"="true")
