-- 订单配送与管理员功能扩展
SET NAMES utf8mb4;
USE xhxt_db;

-- 1. 订单表增加物流字段
ALTER TABLE user_order ADD COLUMN IF NOT EXISTS express_company VARCHAR(64) DEFAULT NULL COMMENT '快递公司' AFTER receiver_address;
ALTER TABLE user_order ADD COLUMN IF NOT EXISTS tracking_no VARCHAR(64) DEFAULT NULL COMMENT '物流单号' AFTER express_company;
ALTER TABLE user_order ADD COLUMN IF NOT EXISTS delivery_time DATETIME DEFAULT NULL COMMENT '发货时间' AFTER pay_time;
ALTER TABLE user_order ADD COLUMN IF NOT EXISTS finish_time DATETIME DEFAULT NULL COMMENT '完成时间' AFTER delivery_time;

-- 2. 用户表支持管理员/员工管理逻辑（确保 status 字段存在）
-- status: 1正常 0禁用/封禁
-- role: ADMIN 管理员, STAFF 员工, USER 用户

-- 3. 系统配置/财务统计暂不建表，通过 SQL 聚合查询实现