DROP TABLE IF EXISTS `ods_ocean_engine_advertising_report`;
CREATE TABLE `ods_ocean_engine_advertising_report`
(
    `id`                                  BIGINT         NOT NULL DEFAULT 0,
    `cdp_project_id`                      BIGINT         NOT NULL DEFAULT 0,
    `cdp_project_name`                    STRING         NOT NULL DEFAULT '',
    `cdp_promotion_id`                    BIGINT         NOT NULL DEFAULT 0,
    `cdp_promotion_name`                  STRING         NOT NULL DEFAULT '',
    `stat_cost`                           DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `show_cnt`                            INT            NOT NULL DEFAULT 0,
    `cpm_platform`                        DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `show_cost`                           DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `click_cnt`                           INT            NOT NULL DEFAULT 0,
    `cpc_platform`                        DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `click_cost`                          DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `attribution_customer_effective_cost` DECIMAL(12, 6) NOT NULL DEFAULT 0.0BD,
    `stat_time_day`                       BIGINT         NOT NULL DEFAULT 0,
    `utc_created`                         BIGINT         NOT NULL DEFAULT 0,
    `utc_modified`                        BIGINT         NOT NULL DEFAULT 0,
    `utc_deleted`                         BIGINT         NOT NULL DEFAULT 0
) tblproperties("transactional"="true");
DROP TABLE IF EXISTS `ods_ocean_engine_advertising`;
CREATE TABLE `ods_ocean_engine_advertising`
(
    `id`                                  BIGINT         NOT NULL DEFAULT 0,
    `cdp_project_id`                      BIGINT         NOT NULL DEFAULT 0,
    `cdp_project_name`                    STRING         NOT NULL DEFAULT '',
    `cdp_promotion_id`                    BIGINT         NOT NULL DEFAULT 0,
    `cdp_promotion_name`                  STRING         NOT NULL DEFAULT '',
    `utc_created`                         BIGINT         NOT NULL DEFAULT 0,
    `utc_modified`                        BIGINT         NOT NULL DEFAULT 0,
    `utc_deleted`                         BIGINT         NOT NULL DEFAULT 0
) tblproperties("transactional"="true")
