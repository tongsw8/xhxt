-- 社区论坛模块：板块、帖子、评论、互动
SET NAMES utf8mb4;
USE xhxt_db;

-- 1. 论坛板块
CREATE TABLE IF NOT EXISTS forum_category (
  id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  category_name VARCHAR(64) NOT NULL COMMENT '板块名称',
  description   VARCHAR(255) DEFAULT NULL COMMENT '板块描述',
  sort_no       INT DEFAULT 0 COMMENT '排序',
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛板块';

-- 2. 论坛帖子
CREATE TABLE IF NOT EXISTS forum_post (
  id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  category_id   BIGINT NOT NULL COMMENT '板块ID',
  user_id       BIGINT NOT NULL COMMENT '发布人ID',
  title         VARCHAR(200) NOT NULL COMMENT '帖子标题',
  content       LONGTEXT NOT NULL COMMENT '帖子内容',
  view_count    INT DEFAULT 0 COMMENT '浏览量',
  is_top        TINYINT DEFAULT 0 COMMENT '是否置顶:1是0否',
  is_best       TINYINT DEFAULT 0 COMMENT '是否加精:1是0否',
  status        TINYINT DEFAULT 1 COMMENT '1正常 0封禁',
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_post_user (user_id),
  KEY idx_post_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子';

-- 3. 通用评论表 (资讯、帖子共用)
CREATE TABLE IF NOT EXISTS comment (
  id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  target_id     BIGINT NOT NULL COMMENT '目标ID(资讯/帖子ID)',
  target_type   VARCHAR(20) NOT NULL COMMENT '类型: NEWS / POST',
  user_id       BIGINT NOT NULL COMMENT '评论人ID',
  parent_id     BIGINT DEFAULT NULL COMMENT '父评论ID(回复用)',
  content       TEXT NOT NULL COMMENT '评论内容',
  is_staff      TINYINT DEFAULT 0 COMMENT '是否官方客服回复:1是0否',
  status        TINYINT DEFAULT 1 COMMENT '1通过 0待审 -1封禁',
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_comment_target (target_id, target_type),
  KEY idx_comment_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 4. 互动行为表 (点赞、收藏)
CREATE TABLE IF NOT EXISTS user_action (
  id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id       BIGINT NOT NULL COMMENT '用户ID',
  target_id     BIGINT NOT NULL COMMENT '目标ID',
  target_type   VARCHAR(20) NOT NULL COMMENT '类型: NEWS / POST / PRODUCT',
  action_type   VARCHAR(20) NOT NULL COMMENT '行为: LIKE(点赞) / COLLECT(收藏)',
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_target_action (user_id, target_id, target_type, action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户互动行为';