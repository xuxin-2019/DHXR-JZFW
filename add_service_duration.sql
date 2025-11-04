-- 为订单表添加服务时长字段
ALTER TABLE `order` 
ADD COLUMN `service_duration` INT DEFAULT NULL COMMENT '服务时长（单位：分钟）';