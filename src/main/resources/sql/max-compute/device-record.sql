DROP TABLE IF EXISTS `ods_event_data`;
CREATE TABLE `ods_event_data`
(
    `id`                       BIGINT NOT NULL DEFAULT 0,
    `event`                    INT    NOT NULL DEFAULT 0,
    `event_name`               STRING NOT NULL DEFAULT '',
    `union_id`                 BIGINT NOT NULL DEFAULT 0,
    `create_time`              BIGINT NOT NULL DEFAULT 0,
    `time_free`                INT    NOT NULL DEFAULT 0,
    `identity_imei`            STRING NOT NULL DEFAULT '',
    `identity_oaid`            STRING NOT NULL DEFAULT '',
    `login_id`                 BIGINT NOT NULL DEFAULT 0,
    `wx_open_id`               STRING NOT NULL DEFAULT '',
    `wx_union_id`              STRING NOT NULL DEFAULT '',
    `identity_android_id`      STRING NOT NULL DEFAULT '',
    `identity_mac`             STRING NOT NULL DEFAULT '',
    `identity_idfa`            STRING NOT NULL DEFAULT '',
    `identity_idfv`            STRING NOT NULL DEFAULT '',
    `identity_mobile`          BIGINT NOT NULL DEFAULT 0,
    `identity_ua`              STRING NOT NULL DEFAULT '',
    `app_version`              STRING NOT NULL DEFAULT '',
    `is_wifi`                  INT    NOT NULL DEFAULT 0,
    `ip`                       STRING NOT NULL DEFAULT '',
    `manufacturer`             STRING NOT NULL DEFAULT '',
    `model`                    STRING NOT NULL DEFAULT '',
    `os`                       STRING NOT NULL DEFAULT '',
    `os_version`               STRING NOT NULL DEFAULT '',
    `screen_width`             INT    NOT NULL DEFAULT 0,
    `screen_height`            INT    NOT NULL DEFAULT 0,
    `traffic_type`             INT    NOT NULL DEFAULT 0,
    `promotion_channel`        INT    NOT NULL DEFAULT 0,
    `promotion_method`         INT    NOT NULL DEFAULT 0,
    `app_type`                 INT    NOT NULL DEFAULT 0,
    `ad_id`                    STRING NOT NULL DEFAULT '',
    `promotion_media`          INT    NOT NULL DEFAULT 0,
    `promotion_id`             STRING NOT NULL DEFAULT '',
    `device_id`                STRING NOT NULL DEFAULT '',
    `item_id`                  BIGINT NOT NULL DEFAULT 0,
    `platform_kind`            INT    NOT NULL DEFAULT 0,
    `type`                     INT    NOT NULL DEFAULT 0,
    `project_id`               BIGINT NOT NULL DEFAULT 0,
    `ad_name`                  STRING NOT NULL DEFAULT '',
    `project_name`             STRING NOT NULL DEFAULT '',
    `mid1`                     STRING NOT NULL DEFAULT '',
    `mid2`                     STRING NOT NULL DEFAULT '',
    `mid3`                     STRING NOT NULL DEFAULT '',
    `mid4`                     STRING NOT NULL DEFAULT '',
    `mid5`                     STRING NOT NULL DEFAULT '',
    `mid6`                     STRING NOT NULL DEFAULT '',
    `aid`                      BIGINT NOT NULL DEFAULT 0,
    `aid_name`                 STRING NOT NULL DEFAULT '',
    `cid`                      BIGINT NOT NULL DEFAULT 0,
    `cid_name`                 STRING NOT NULL DEFAULT '',
    `campaign_id`              BIGINT NOT NULL DEFAULT 0,
    `campaign_name`            STRING NOT NULL DEFAULT '',
    `c_type`                   INT    NOT NULL DEFAULT 0,
    `advertiser_id`            BIGINT NOT NULL DEFAULT 0,
    `c_site`                   INT    NOT NULL DEFAULT 0,
    `convert_id`               BIGINT NOT NULL DEFAULT 0,
    `request_id`               STRING NOT NULL DEFAULT '',
    `track_id`                 STRING NOT NULL DEFAULT '',
    `sl`                       STRING NOT NULL DEFAULT '',
    `idfa_md5`                 STRING NOT NULL DEFAULT '',
    `o_aid_md5`                STRING NOT NULL DEFAULT '',
    `mac1`                     STRING NOT NULL DEFAULT '',
    `ipv4`                     STRING NOT NULL DEFAULT '',
    `ipv6`                     STRING NOT NULL DEFAULT '',
    `geo`                      STRING NOT NULL DEFAULT '',
    `ts`                       BIGINT NOT NULL DEFAULT 0,
    `callback_param`           STRING NOT NULL DEFAULT '',
    `callback_url`             STRING NOT NULL DEFAULT '',
    `union_site`               BIGINT NOT NULL DEFAULT 0,
    `c_aid`                    STRING NOT NULL DEFAULT '',
    `c_aid_md5`                STRING NOT NULL DEFAULT '',
    `product_id`               STRING NOT NULL DEFAULT '',
    `outer_id`                 STRING NOT NULL DEFAULT '',
    `click_id`                 STRING NOT NULL DEFAULT '',
    `click_time`               BIGINT NOT NULL DEFAULT 0,
    `impression_time`          BIGINT NOT NULL DEFAULT 0,
    `creative_components_info` STRING NOT NULL DEFAULT '',
    `element_info`             STRING NOT NULL DEFAULT '',
    `marketing_goal`           STRING NOT NULL DEFAULT '',
    `marketing_sub_goal`       STRING NOT NULL DEFAULT '',
    `marketing_target_id`      INT    NOT NULL DEFAULT 0,
    `marketing_carrier_id`     BIGINT NOT NULL DEFAULT 0,
    `marketing_sub_carrier_id` STRING NOT NULL DEFAULT '',
    `marketing_asset_id`       INT    NOT NULL DEFAULT 0,
    `material_package_id`      STRING NOT NULL DEFAULT '',
    `ad_platform_type`         INT    NOT NULL DEFAULT 0,
    `ad_type`                  INT    NOT NULL DEFAULT 0,
    `agency_id`                INT    NOT NULL DEFAULT 0,
    `click_sku_id`             STRING NOT NULL DEFAULT '',
    `billing_event`            INT    NOT NULL DEFAULT 0,
    `deeplink_url`             STRING NOT NULL DEFAULT '',
    `universal_link`           STRING NOT NULL DEFAULT '',
    `page_url`                 STRING NOT NULL DEFAULT '',
    `process_time`             BIGINT NOT NULL DEFAULT 0,
    `promoted_object_id`       STRING NOT NULL DEFAULT '',
    `impression_id`            STRING NOT NULL DEFAULT '',
    `encrypted_position_id`    INT    NOT NULL DEFAULT 0,
    `q_aid_c_aid`              STRING NOT NULL DEFAULT '',
    `ip_md5`                   STRING NOT NULL DEFAULT '',
    `ipv6_md5`                 STRING NOT NULL DEFAULT '',
    `channel_package_id`       STRING NOT NULL DEFAULT '',
    `act_type`                 STRING NOT NULL DEFAULT '',
    `act_time`                 BIGINT NOT NULL DEFAULT 0,
    `status`                   INT    NOT NULL DEFAULT 0,
    `imei_md5`                 STRING NOT NULL DEFAULT '',
    `oaid1`                    STRING NOT NULL DEFAULT '',
    `idfa`                     STRING NOT NULL DEFAULT '',
    `idfa1`                    STRING NOT NULL DEFAULT '',
    `android_id`               STRING NOT NULL DEFAULT '',
    `android_id_md5`           STRING NOT NULL DEFAULT '',
    `ua1`                      STRING NOT NULL DEFAULT '',
    `readds`                   STRING NOT NULL DEFAULT '',
    `custid1`                  STRING NOT NULL DEFAULT '',
    `timestamp`                BIGINT NOT NULL DEFAULT 0,
    `app_key`                  STRING NOT NULL DEFAULT '',
    `app_name`                 STRING NOT NULL DEFAULT '',
    `unit_id`                  BIGINT NOT NULL DEFAULT 0,
    `creativity_id`            BIGINT NOT NULL DEFAULT 0,
    `content`                  STRING NOT NULL DEFAULT '',
    `red_id`                   STRING NOT NULL DEFAULT '',
    `paid`                     STRING NOT NULL DEFAULT '',
    `placement`                INT    NOT NULL DEFAULT 0
) tblproperties("transactional"="true");
DROP TABLE IF EXISTS `ods_device_user_info`;
CREATE TABLE `ods_device_user_info`
(
    `id`                  BIGINT NOT NULL DEFAULT 0,
    `login_id`            STRING NOT NULL DEFAULT '',
    `event_data_id`       BIGINT NOT NULL DEFAULT 0,
    `identity_mobile`     BIGINT NOT NULL DEFAULT 0,
    `wx_open_id`          STRING NOT NULL DEFAULT '',
    `wx_union_id`         STRING NOT NULL DEFAULT '',
    `identity_imei`       STRING NOT NULL DEFAULT '',
    `identity_oaid`       STRING NOT NULL DEFAULT '',
    `identity_android_id` STRING NOT NULL DEFAULT '',
    `identity_mac`        STRING NOT NULL DEFAULT '',
    `identity_idfa`       STRING NOT NULL DEFAULT '',
    `identity_idfv`       STRING NOT NULL DEFAULT '',
    `identity_ua`         STRING NOT NULL DEFAULT '',
    `app_version`         STRING NOT NULL DEFAULT '',
    `ip`                  STRING NOT NULL DEFAULT '',
    `manufacturer`        STRING NOT NULL DEFAULT '',
    `model`               STRING NOT NULL DEFAULT '',
    `os`                  STRING NOT NULL DEFAULT '',
    `device_id`           STRING NOT NULL DEFAULT '',
    `os_version`          STRING NOT NULL DEFAULT '',
    `platform_kind`       INT    NOT NULL DEFAULT 0
) tblproperties("transactional"="true")
