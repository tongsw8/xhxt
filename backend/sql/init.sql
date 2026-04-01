-- xhxt 数据库初始化脚本（全字段下划线版）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS xhxt_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE xhxt_db;

DROP TABLE IF EXISTS users;
CREATE TABLE users (
  id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  account     VARCHAR(64) NOT NULL COMMENT '登录账号',
  password    VARCHAR(128) NOT NULL COMMENT '密码(MD5)',
  nickname    VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  real_name   VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
  phone       VARCHAR(32) DEFAULT NULL COMMENT '手机号',
  email       VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  gender      TINYINT DEFAULT 0 COMMENT '性别:0未知 1男 2女',
  role        VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/STAFF/USER',
  status      TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
  reg_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_account (account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户与登录';

INSERT INTO users (account, password, nickname, role, status, reg_time) VALUES
('admin', '0192023a7bbd73250516f069df18b500', '管理员', 'ADMIN', 1, NOW()),
('staff', '0192023a7bbd73250516f069df18b500', '员工', 'STAFF', 1, NOW()),
('user', '0192023a7bbd73250516f069df18b500', '普通用户', 'USER', 1, NOW());

SET FOREIGN_KEY_CHECKS = 1;
