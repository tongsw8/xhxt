-- 订单模块初始化脚本（下划线字段版）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE xhxt_db;

CREATE TABLE IF NOT EXISTS user_order (
  id               BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  order_no         VARCHAR(64) NOT NULL COMMENT '订单号',
  user_id          BIGINT NOT NULL COMMENT '用户ID',
  total_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  status           TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0待支付 1已支付 2已关闭',
  expire_time      DATETIME NOT NULL COMMENT '过期时间(30分钟)',
  pay_time         DATETIME NULL DEFAULT NULL COMMENT '支付时间',
  close_time       DATETIME NULL DEFAULT NULL COMMENT '关闭时间',
  receiver_name    VARCHAR(64) DEFAULT NULL COMMENT '收货人',
  receiver_phone   VARCHAR(32) DEFAULT NULL COMMENT '收货电话',
  receiver_address VARCHAR(255) DEFAULT NULL COMMENT '收货地址快照',
  create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_order_user (user_id),
  KEY idx_order_status (status),
  KEY idx_order_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户订单';

CREATE TABLE IF NOT EXISTS user_order_item (
  id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  order_id      BIGINT NOT NULL COMMENT '订单ID',
  product_id    BIGINT NOT NULL COMMENT '商品ID',
  product_name  VARCHAR(100) NOT NULL COMMENT '下单时商品名快照',
  product_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '下单时单价快照',
  quantity      INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  cover_img     VARCHAR(255) DEFAULT NULL COMMENT '下单时主图快照',
  create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_item_order (order_id),
  KEY idx_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

SET FOREIGN_KEY_CHECKS = 1;
