// payment.js

/**
 * 支付页面
 * 处理微信支付流程
 */

// 导入API路由管理
import { API, request } from '../../utils/api';
Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 订单信息
    orderInfo: null,
    // 支付参数
    paymentParams: null,
    // 加载状态
    loading: false,
    // 错误信息
    errorMsg: '',
    // 支付状态
    paymentStatus: 0, // 0: 未支付, 1: 支付中, 2: 支付成功, 3: 支付失败
    // 倒计时
    countdown: 900, // 15分钟倒计时
    countdownText: ''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    // 获取页面参数
    const orderData = JSON.parse(options.orderData || '{}');
    this.setData({ orderInfo: orderData });
    
    // 立即创建支付订单
    this.createPaymentOrder();
    
    // 开始倒计时
    this.startCountdown();
  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function() {
    // 清除倒计时
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
    }
  },

  /**
   * 创建支付订单
   */
  createPaymentOrder: function() {
    const { orderInfo } = this.data;
    
    this.setData({ loading: true, errorMsg: '' });
    
    request(API.payment.create, {
      method: 'POST',
      data: {
        serviceTypeId: orderInfo.serviceTypeId,
        totalAmount: orderInfo.totalAmount,
        serviceAddress: orderInfo.serviceAddress,
        serviceTime: orderInfo.serviceTime,
        serviceDuration: orderInfo.serviceDuration
      }
    }).then(res => {
      this.setData({ loading: false });
      
      if (res.code === 200 && res.data) {
        // 保存订单ID和支付参数
        this.setData({
          orderInfo: {
            ...orderInfo,
            orderId: res.data.orderId,
            orderNo: res.data.orderNo
          },
          paymentParams: res.data.paymentParams
        });
        
        // 调用微信支付
        this.invokeWechatPayment();
      } else {
        this.setData({
          errorMsg: res.message || '创建支付订单失败',
          paymentStatus: 3
        });
      }
    }).catch(err => {
      console.error('创建支付订单失败:', err);
      this.setData({
        loading: false,
        errorMsg: '网络异常，请稍后重试',
        paymentStatus: 3
      });
    });
  },

  /**
   * 调用微信支付
   */
  invokeWechatPayment: function() {
    const { paymentParams } = this.data;
    
    if (!paymentParams) {
      this.setData({
        errorMsg: '支付参数错误',
        paymentStatus: 3
      });
      return;
    }
    
    this.setData({ paymentStatus: 1 });
    
    wx.requestPayment({
      timeStamp: paymentParams.timeStamp,
      nonceStr: paymentParams.nonceStr,
      package: paymentParams.package,
      signType: paymentParams.signType,
      paySign: paymentParams.paySign,
      success: (res) => {
        // 支付成功
        this.setData({ paymentStatus: 2 });
        // 跳转到支付结果页面
        setTimeout(() => {
          wx.redirectTo({
            url: `/pages/payment-result/payment-result?status=success&orderId=${this.data.orderInfo.orderId}&orderNo=${this.data.orderInfo.orderNo}`
          });
        }, 1500);
      },
      fail: (err) => {
        console.error('微信支付失败:', err);
        // 用户取消支付不算失败
        if (err.errMsg.indexOf('cancel') === -1) {
          this.setData({
            errorMsg: '支付失败，请稍后重试',
            paymentStatus: 3
          });
        } else {
          this.setData({ paymentStatus: 0 });
        }
      },
      complete: () => {
        // 清除倒计时
        if (this.countdownTimer) {
          clearInterval(this.countdownTimer);
        }
      }
    });
  },

  /**
   * 重新支付
   */
  onRetryPayment: function() {
    this.createPaymentOrder();
  },

  /**
   * 关闭支付
   */
  onClosePayment: function() {
    const { orderInfo } = this.data;
    
    if (!orderInfo.orderId) {
      wx.navigateBack();
      return;
    }
    
    // 调用关闭支付接口
    request(`${API.payment.close}/${orderInfo.orderId}`, {
      method: 'POST'
    }).then(() => {
      wx.navigateBack();
    }).catch(() => {
      // 即使关闭接口失败也返回上一页
      wx.navigateBack();
    });
  },

  /**
   * 开始倒计时
   */
  startCountdown: function() {
    this.countdownTimer = setInterval(() => {
      let { countdown } = this.data;
      countdown--;
      
      if (countdown <= 0) {
        clearInterval(this.countdownTimer);
        this.setData({
          countdownText: '支付已超时',
          paymentStatus: 3,
          errorMsg: '支付已超时，请重新下单'
        });
        return;
      }
      
      const minutes = Math.floor(countdown / 60);
      const seconds = countdown % 60;
      this.setData({
        countdown,
        countdownText: `支付剩余时间：${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
      });
    }, 1000);
  }
});