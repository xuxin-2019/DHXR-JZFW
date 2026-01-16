-- 数据库初始化脚本
-- 创建时间: 2026-01-13

-- 1. 创建服务类型表 (service_type)
CREATE TABLE service_type (
    id BIGSERIAL PRIMARY KEY COMMENT '服务类型ID',
    name VARCHAR(100) NOT NULL COMMENT '服务名称',
    description TEXT COMMENT '服务描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '服务价格',
    duration INTEGER NOT NULL COMMENT '服务时长(分钟)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 2. 创建用户表 (app_user)
CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY COMMENT '用户ID',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    name VARCHAR(100) COMMENT '姓名',
    address VARCHAR(255) COMMENT '地址',
    openid VARCHAR(100) UNIQUE COMMENT '微信openid',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 3. 创建护工表 (nurse)
CREATE TABLE nurse (
    id BIGSERIAL PRIMARY KEY COMMENT '护工ID',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    age INTEGER COMMENT '年龄',
    service_type_id BIGINT REFERENCES service_type(id) COMMENT '服务类型ID',
    openid VARCHAR(100) UNIQUE COMMENT '微信openid',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    status INTEGER DEFAULT 1 COMMENT '状态(1:空闲, 2:忙碌, 3:离线)',
    rating DECIMAL(3, 1) DEFAULT 0.0 COMMENT '评分',
    service_count INTEGER DEFAULT 0 COMMENT '服务次数',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 4. 创建管理员表 (admin)
CREATE TABLE admin (
    id BIGSERIAL PRIMARY KEY COMMENT '管理员ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 5. 创建订单表 ("order")
CREATE TABLE "order" (
    id BIGSERIAL PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT REFERENCES app_user(id) COMMENT '用户ID',
    nurse_id BIGINT REFERENCES nurse(id) COMMENT '护工ID',
    service_type_id BIGINT REFERENCES service_type(id) COMMENT '服务类型ID',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '总金额',
    status INTEGER DEFAULT 0 COMMENT '订单状态(0:待支付, 1:待派单, 2:已派单, 3:已接单, 4:服务中, 5:已完成, 6:已取消, 7:已拒绝)',
    service_address VARCHAR(255) NOT NULL COMMENT '服务地址',
    service_time VARCHAR(20) COMMENT '服务时间',
    payment_time VARCHAR(20) COMMENT '支付时间',
    start_time VARCHAR(20) COMMENT '服务开始时间',
    end_time VARCHAR(20) COMMENT '服务结束时间',
    service_duration INTEGER COMMENT '服务时长（单位：分钟）',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 6. 创建支付记录表 (payment_record)
CREATE TABLE payment_record (
    id BIGSERIAL PRIMARY KEY COMMENT '支付记录ID',
    order_id BIGINT REFERENCES "order"(id) COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单编号',
    amount DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    status INTEGER DEFAULT 1 COMMENT '支付状态：1-待支付，2-支付中，3-支付成功，4-支付失败',
    transaction_id VARCHAR(100) UNIQUE COMMENT '微信支付交易号',
    out_trade_no VARCHAR(100) UNIQUE COMMENT '微信支付商户订单号',
    pay_time TIMESTAMP COMMENT '支付时间',
    pay_type INTEGER DEFAULT 1 COMMENT '支付方式：1-微信支付',
    user_id BIGINT REFERENCES app_user(id) COMMENT '用户ID',
    callback_result TEXT COMMENT '支付回调结果',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 7. 创建退款申请表 (refund_application)
CREATE TABLE refund_application (
    id BIGSERIAL PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT REFERENCES "order"(id) COMMENT '订单ID',
    payment_id BIGINT REFERENCES payment_record(id) COMMENT '支付记录ID',
    refund_no VARCHAR(50) NOT NULL UNIQUE COMMENT '退款单号',
    transaction_id VARCHAR(100) COMMENT '微信支付交易号',
    out_refund_no VARCHAR(100) UNIQUE COMMENT '商户退款单号',
    refund_amount DECIMAL(10, 2) NOT NULL COMMENT '退款金额',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    reason TEXT NOT NULL COMMENT '退款原因',
    status INTEGER DEFAULT 1 COMMENT '退款状态 1:申请中 2:已通过 3:已拒绝 4:退款中 5:退款成功 6:退款失败',
    admin_id BIGINT REFERENCES admin(id) COMMENT '管理员ID',
    remark TEXT COMMENT '审核备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 8. 创建评价表 (evaluation)
CREATE TABLE evaluation (
    id BIGSERIAL PRIMARY KEY COMMENT '评价ID',
    order_id BIGINT REFERENCES "order"(id) COMMENT '订单ID',
    user_id BIGINT REFERENCES app_user(id) COMMENT '用户ID',
    nurse_id BIGINT REFERENCES nurse(id) COMMENT '护工ID',
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5) COMMENT '评分(1-5)',
    content TEXT COMMENT '评价内容',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 9. 创建通知表 (notification)
CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY COMMENT '通知ID',
    user_id BIGINT REFERENCES app_user(id) COMMENT '用户ID',
    nurse_id BIGINT REFERENCES nurse(id) COMMENT '护工ID',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建索引
CREATE INDEX idx_order_user_id ON "order"(user_id);
CREATE INDEX idx_order_nurse_id ON "order"(nurse_id);
CREATE INDEX idx_order_status ON "order"(status);
CREATE INDEX idx_payment_order_id ON payment_record(order_id);
CREATE INDEX idx_payment_status ON payment_record(status);
CREATE INDEX idx_refund_order_id ON refund_application(order_id);
CREATE INDEX idx_refund_status ON refund_application(status);
CREATE INDEX idx_evaluation_nurse_id ON evaluation(nurse_id);
CREATE INDEX idx_nurse_service_type_id ON nurse(service_type_id);
CREATE INDEX idx_nurse_status ON nurse(status);

-- 插入初始数据
-- 插入服务类型数据
INSERT INTO service_type (name, description, price, duration) VALUES
('居家护理', '专业护理人员上门提供基础护理服务', 120.00, 60),
('康复训练', '针对术后或慢性病患者的康复训练服务', 150.00, 90),
('日常照料', '提供日常生活照料和陪伴服务', 80.00, 120),
('特殊护理', '针对特殊人群的专业护理服务', 200.00, 180);

-- 插入管理员初始数据 (密码: admin123, 已加密)
INSERT INTO admin (username, password, name) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM2QzvH8fQskdjfksdfa', '系统管理员');

-- 插入测试用户数据
INSERT INTO app_user (phone, name, address, openid) VALUES
('13800138001', '张三', '北京市朝阳区', 'o1qXt5B_xxxxxx'),
('13800138002', '李四', '上海市浦东新区', 'o1qXt5B_yyyyyy');

-- 插入测试护工数据
INSERT INTO nurse (phone, name, age, service_type_id, status, rating, service_count) VALUES
('13900139001', '王五', 35, 1, 1, 4.8, 120),
('13900139002', '赵六', 42, 2, 1, 4.9, 150);

-- 插入测试订单数据
INSERT INTO "order" (order_no, user_id, nurse_id, service_type_id, total_amount, status, service_address, service_time) VALUES
('ORDER20260113000001', 1, 1, 1, 120.00, 5, '北京市朝阳区XX街道XX小区1号楼1单元101室', '2026-01-13 10:00'),
('ORDER20260113000002', 2, 2, 2, 150.00, 3, '上海市浦东新区XX路XX号', '2026-01-14 14:00');

-- 插入测试支付记录数据
INSERT INTO payment_record (order_id, order_no, amount, status, transaction_id, out_trade_no, pay_time, user_id) VALUES
(1, 'ORDER20260113000001', 120.00, 3, '420000183220260113XXXXXXXXXXXX', 'PAY20260113000001', '2026-01-13 09:55:30', 1),
(2, 'ORDER20260113000002', 150.00, 3, '420000183220260113YYYYYYYYYYYY', 'PAY20260113000002', '2026-01-13 13:45:20', 2);

-- 插入测试评价数据
INSERT INTO evaluation (order_id, user_id, nurse_id, rating, content) VALUES
(1, 1, 1, 5, '护工服务态度很好，专业水平高，非常满意！');

-- 插入测试通知数据
INSERT INTO notification (user_id, title, content) VALUES
(1, '服务完成提醒', '您的居家护理服务已完成，请对服务进行评价。'),
(2, '订单已派单', '您的康复训练服务已派单，护工将按时上门服务。');

COMMIT;
