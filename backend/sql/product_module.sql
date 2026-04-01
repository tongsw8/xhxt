-- 商品管理模块初始化脚本（全字段下划线版）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE xhxt_db;

CREATE TABLE IF NOT EXISTS product_category (
  id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
  sort_no       INT NOT NULL DEFAULT 0 COMMENT '排序号',
  status        TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用 0停用',
  create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_category_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

CREATE TABLE IF NOT EXISTS product_info (
  id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  category_id   BIGINT NOT NULL COMMENT '分类ID',
  product_name  VARCHAR(100) NOT NULL COMMENT '商品名称',
  price         DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格',
  stock         INT NOT NULL DEFAULT 0 COMMENT '库存',
  cover_img     VARCHAR(255) DEFAULT NULL COMMENT '主图',
  detail_text   TEXT COMMENT '详情',
  status        TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1上架 0下架',
  create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_product_category (category_id),
  KEY idx_product_status (status),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息';

SET FOREIGN_KEY_CHECKS = 1;
