-- 将订单表中的order_id字段类型修改为varchar(50)
ALTER TABLE `order` MODIFY COLUMN `order_id` VARCHAR(50) COMMENT '订单ID';

-- 如果evaluation表中有外键关联到order_id，也需要同步修改
ALTER TABLE `evaluation` MODIFY COLUMN `order_id` VARCHAR(50) COMMENT '订单ID';