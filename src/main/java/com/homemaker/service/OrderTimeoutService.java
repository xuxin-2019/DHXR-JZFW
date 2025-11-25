package com.homemaker.service;

import com.homemaker.entity.Order;
import com.homemaker.entity.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 订单超时自动取消服务
 * 定时检查并取消超过15分钟未支付的订单
 */
@Service
public class OrderTimeoutService {

    private static final Logger logger = LoggerFactory.getLogger(OrderTimeoutService.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    /**
     * 定时任务：每分钟检查一次超时未支付的订单
     * cron表达式：每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void cancelTimeoutOrders() {
        logger.info("开始执行订单超时检查任务: {}", new Date());

        try {
            // 获取所有等待支付的订单（状态为待支付，支付状态为等待支付）
            List<Order> pendingOrders = orderService.findPendingPaymentOrders();
            logger.info("当前待支付订单数量: {}", pendingOrders.size());

            // 检查每个订单是否超时（超过15分钟）
            for (Order order : pendingOrders) {
                if (isOrderTimeout(order.getCreateTime())) {
                    logger.info("订单{}已超时，开始自动取消", order.getId());
                    
                    // 查询对应的支付记录
                    Payment payment = paymentService.getPaymentByOrderId(order.getId());
                    if (payment != null && payment.getStatus() == 2) { // 支付状态为等待支付
                        // 更新支付状态为支付失败(4)
                        payment.setStatus(4);
                        payment.setUpdateTime(new Date());
                        paymentService.updateById(payment);
                        logger.info("订单{}支付状态已更新为支付失败", order.getId());
                    }
                    
                    // 更新订单状态为已取消(6)
                    order.setStatus(6);
                    order.setUpdateTime(new Date());
                    orderService.updateById(order);
                    logger.info("订单{}状态已更新为已取消", order.getId());
                }
            }

            logger.info("订单超时检查任务执行完成: {}", new Date());
        } catch (Exception e) {
            logger.error("执行订单超时检查任务时发生错误", e);
        }
    }

    /**
     * 判断订单是否超时（超过15分钟）
     * @param createTime 订单创建时间
     * @return 是否超时
     */
    private boolean isOrderTimeout(Date createTime) {
        Date now = new Date();
        // 计算订单创建时间加上15分钟后的时间（15分钟 = 15 * 60 * 1000毫秒）
        long timeoutTimeMillis = createTime.getTime() + 15 * 60 * 1000;
        // 如果当前时间已经超过超时时间，则订单超时
        return now.getTime() > timeoutTimeMillis;
    }
}