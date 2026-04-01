-- 购物车/收藏模块初始化脚本（全字段下划线版）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE xhxt_db;

CREATE TABLE IF NOT EXISTS user_cart (
  id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id     BIGINT NOT NULL COMMENT '用户ID',
  product_id  BIGINT NOT NULL COMMENT '商品ID',
  quantity    INT NOT NULL DEFAULT 1 COMMENT '数量',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_product (user_id, product_id),
  KEY idx_cart_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户购物车';

CREATE TABLE IF NOT EXISTS user_favorite (
  id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id     BIGINT NOT NULL COMMENT '用户ID',
  product_id  BIGINT NOT NULL COMMENT '商品ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_fav_product (user_id, product_id),
  KEY idx_favorite_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏';

SET FOREIGN_KEY_CHECKS = 1;
