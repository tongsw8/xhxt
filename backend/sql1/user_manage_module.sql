-- 管理员与员工管理
SET NAMES utf8mb4;
USE xhxt_db;

-- users 表字段已在之前 init.sql 补全，此处仅确保逻辑一致性
-- 增加封禁状态支持 (status: 1正常 0禁用/封禁)
-- 初始化一个默认员工用于测试
INSERT INTO users (account, password, nickname, role, status, reg_time) 
SELECT 'staff01', '0192023a7bbd73250516f069df18b500', '花卉园艺师', 'STAFF', 1, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE account = 'staff01');
