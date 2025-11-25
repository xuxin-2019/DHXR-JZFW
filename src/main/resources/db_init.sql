-- 创建数据库
CREATE DATABASE IF NOT EXISTS home_make_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE home_make_db;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 护工表
CREATE TABLE IF NOT EXISTS `nurse` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '护工ID',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `age` int(3) DEFAULT NULL COMMENT '年龄',
  `service_type_id` bigint(20) NOT NULL COMMENT '服务类型ID',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态(1:空闲, 2:忙碌, 3:离线)',
  `rating` decimal(3,2) DEFAULT 5.00 COMMENT '评分',
  `service_count` int(11) DEFAULT 0 COMMENT '服务次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=20001 DEFAULT CHARSET=utf8mb4 COMMENT='护工表';

-- 管理员表
CREATE TABLE IF NOT EXISTS `admin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=30001 DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 服务类型表
CREATE TABLE IF NOT EXISTS `service_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '服务类型ID',
  `name` varchar(50) NOT NULL COMMENT '服务名称',
  `description` varchar(255) DEFAULT NULL COMMENT '服务描述',
  `price` decimal(10,2) NOT NULL COMMENT '服务价格',
  `duration` int(11) NOT NULL COMMENT '服务时长(分钟)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40001 DEFAULT CHARSET=utf8mb4 COMMENT='服务类型表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `nurse_id` bigint(20) DEFAULT NULL COMMENT '护工ID',
  `service_type_id` bigint(20) NOT NULL COMMENT '服务类型ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总金额',
  `status` int(2) NOT NULL DEFAULT 0 COMMENT '订单状态(0:待支付, 1:待派单, 2:已派单, 3:已接单, 4:服务中, 5:已完成, 6:已取消, 7:已拒绝)',
  `service_address` varchar(255) NOT NULL COMMENT '服务地址',
  `service_time` varchar(50) NOT NULL COMMENT '服务时间',
  `payment_time` varchar(50) DEFAULT NULL COMMENT '支付时间',
  `payment_order_id` varchar(100) DEFAULT NULL COMMENT '支付订单ID',
  `refund_order_id` varchar(100) DEFAULT NULL COMMENT '退款订单ID',
  `transaction_id` varchar(100) DEFAULT NULL COMMENT '微信交易号',
  `expire_time` datetime DEFAULT NULL COMMENT '订单过期时间',
  `start_time` varchar(50) DEFAULT NULL COMMENT '服务开始时间',
  `end_time` varchar(50) DEFAULT NULL COMMENT '服务结束时间',
  `service_duration` int(11) NOT NULL COMMENT '服务时长（单位：分钟）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=50001 DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 评价表
CREATE TABLE IF NOT EXISTS `evaluation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` VARCHAR(50) NOT NULL COMMENT '订单ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `nurse_id` bigint(20) NOT NULL COMMENT '护工ID',
  `rating` tinyint(1) NOT NULL COMMENT '评分(1-5星)',
  `content` varchar(255) DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=60001 DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `nurse_id` bigint(20) DEFAULT NULL COMMENT '护工ID',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` varchar(255) NOT NULL COMMENT '通知内容',
  `type` tinyint(1) NOT NULL COMMENT '通知类型(1:订单通知, 2:系统通知)',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读(0:未读, 1:已读)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=70001 DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 插入管理员初始数据
INSERT INTO `admin` (`username`, `password`, `name`) VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员');

-- 插入服务类型初始数据
INSERT INTO `service_type` (`name`, `description`, `price`, `duration`) VALUES 
('日常保洁', '家庭日常清洁服务', 120.00, 120),
('深度保洁', '全面细致的清洁服务', 200.00, 180),
('照顾老人', '老人日常生活照料', 180.00, 240),
('照顾儿童', '儿童日常看护服务', 150.00, 180),
('钟点工', '按小时计费的家政服务', 50.00, 60);

-- 扩展用户表，添加openid字段
ALTER TABLE `user` 
ADD COLUMN `openid` varchar(100) DEFAULT NULL COMMENT '微信openid' AFTER `avatar_url`;

-- 为用户表的openid字段添加唯一索引
ALTER TABLE `user` 
ADD UNIQUE KEY `uk_openid` (`openid`);

-- 扩展护工表，添加openid字段
ALTER TABLE `nurse` 
ADD COLUMN `openid` varchar(100) DEFAULT NULL COMMENT '微信openid' AFTER `avatar_url`;

-- 为护工表的openid字段添加唯一索引
ALTER TABLE `nurse` 
ADD UNIQUE KEY `uk_openid` (`openid`);

-- 支付记录表
CREATE TABLE IF NOT EXISTS `payment_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `pay_type` int(2) NOT NULL COMMENT '支付方式：1-微信支付',
  `out_trade_no` varchar(100) DEFAULT NULL COMMENT '微信支付商户订单号',
  `transaction_id` varchar(100) DEFAULT NULL COMMENT '微信支付交易号',
  `status` int(2) NOT NULL COMMENT '支付状态：1-待支付，2-支付中，3-支付成功，4-支付失败',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `callback_result` text DEFAULT NULL COMMENT '支付回调结果',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=80001 DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 退款申请表
CREATE TABLE IF NOT EXISTS `refund_application` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `refund_amount` decimal(10,2) NOT NULL COMMENT '退款金额',
  `refund_reason` varchar(500) NOT NULL COMMENT '退款原因',
  `application_time` datetime NOT NULL COMMENT '申请时间',
  `audit_status` int(2) NOT NULL DEFAULT 0 COMMENT '审核状态(0:待审核,1:审核通过,2:审核拒绝)',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `auditor_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `refund_status` int(2) NOT NULL DEFAULT 0 COMMENT '退款状态(0:未退款,1:退款中,2:退款成功,3:退款失败)',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_order_id` varchar(100) DEFAULT NULL COMMENT '退款订单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_auditor_id` (`auditor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=90001 DEFAULT CHARSET=utf8mb4 COMMENT='退款申请表';