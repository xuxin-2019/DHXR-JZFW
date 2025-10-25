-- 删除用户表中的password字段
ALTER TABLE user DROP COLUMN password;

-- 删除护工表中的password字段
ALTER TABLE nurse DROP COLUMN password;