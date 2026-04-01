-- 把旧版驼峰列名迁移为下划线列名
-- 执行方式：mysql -uroot -p xhxt_db < sql/migrate_to_snake_case.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE xhxt_db;

-- users.regTime -> reg_time
SET @has_regTime := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'regTime'
);
SET @has_reg_time := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'reg_time'
);
SET @sql := IF(@has_regTime > 0 AND @has_reg_time = 0,
  'ALTER TABLE users CHANGE COLUMN regTime reg_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''注册时间''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- product_category
SET @has_categoryName := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'product_category' AND column_name = 'categoryName'
);
SET @has_sortNo := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'product_category' AND column_name = 'sortNo'
);
SET @has_createTime := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'product_category' AND column_name = 'createTime'
);
SET @has_updateTime := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'product_category' AND column_name = 'updateTime'
);

SET @sql := IF(@has_categoryName > 0,
  'ALTER TABLE product_category CHANGE COLUMN categoryName category_name VARCHAR(64) NOT NULL COMMENT ''分类名称''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_sortNo > 0,
  'ALTER TABLE product_category CHANGE COLUMN sortNo sort_no INT NOT NULL DEFAULT 0 COMMENT ''排序号''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_createTime > 0,
  'ALTER TABLE product_category CHANGE COLUMN createTime create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_updateTime > 0,
  'ALTER TABLE product_category CHANGE COLUMN updateTime update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- product_info
SET @has_categoryId := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'product_info' AND column_name = 'categoryId'
);
SET @has_productName := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'product_info' AND column_name = 'productName'
);
SET @has_coverImg := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'product_info' AND column_name = 'coverImg'
);
SET @has_detailText := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'product_info' AND column_name = 'detailText'
);
SET @has_createTime2 := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'product_info' AND column_name = 'createTime'
);
SET @has_updateTime2 := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'product_info' AND column_name = 'updateTime'
);

SET @sql := IF(@has_categoryId > 0,
  'ALTER TABLE product_info CHANGE COLUMN categoryId category_id BIGINT NOT NULL COMMENT ''分类ID''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_productName > 0,
  'ALTER TABLE product_info CHANGE COLUMN productName product_name VARCHAR(100) NOT NULL COMMENT ''商品名称''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_coverImg > 0,
  'ALTER TABLE product_info CHANGE COLUMN coverImg cover_img VARCHAR(255) NULL COMMENT ''主图''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_detailText > 0,
  'ALTER TABLE product_info CHANGE COLUMN detailText detail_text TEXT NULL COMMENT ''详情''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_createTime2 > 0,
  'ALTER TABLE product_info CHANGE COLUMN createTime create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_updateTime2 > 0,
  'ALTER TABLE product_info CHANGE COLUMN updateTime update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
