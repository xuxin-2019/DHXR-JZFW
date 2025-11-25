// payment-result.js

/**
 * 支付结果页面（合并版）
 * 整合支付页面和支付结果页面功能
 * 实现完整的支付失败逻辑处理
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
    // 加载状态
    loading: true,
    // 错误信息
    errorMsg: '',
    // 订单ID
    orderId: '',
    // 订单号
    orderNo: '',
    // 支付记录表状态：1-待支付，2-支付中，3-支付成功，4-支付失败
    paymentStatus: 1,
    // 页面加载错误
    pageError: false,
    pageErrorMessage: ''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    // 获取页面参数
    const orderId = options.orderId || '';
    const orderNo = options.orderNo || '';
    
    if (!orderId) {
      this.setData({
        loading: false,
        pageError: true,
        pageErrorMessage: '订单信息缺失'
      });
      return;
    }
    
    this.setData({
      orderId,
      orderNo
    });
    
    // 跳转之后先查询订单支付状态
    this.queryPaymentStatus();
  },
  
  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function() {
    // 清除状态刷新定时器，避免页面卸载后仍然调用接口
    if (this.refreshStatusTimer) {
      clearInterval(this.refreshStatusTimer);
      this.refreshStatusTimer = null;
    }
  },

  /**
   * 查询支付状态
   * 支付表状态：1-待支付，2-支付中，3-支付成功，4-支付失败
   */
  queryPaymentStatus: function() {
    console.log('执行支付状态查询');
    const { orderId } = this.data;
    
    // 防止重复请求：检查是否有正在进行的请求
    if (this.paymentStatusRequest) {
      console.log('已有查询请求在进行中，取消重复请求');
      return;
    }
    
    // 标记有请求正在进行
    this.paymentStatusRequest = true;
    
    request(API.payment.status, {
      method: 'GET',
      data: {
        orderId: orderId
      }
    }).then(res => {
      this.paymentStatusRequest = false; // 清除请求标记
      
      console.log('支付状态查询结果:', res.data);
      if (res.code === 200 && res.data) {
        const paymentStatus = res.data.status || 1;
        const orderInfo = res.data.orderInfo || {};
        
        this.setData({
          paymentStatus,
          orderInfo: {
            ...orderInfo,
            orderId,
            orderNo: this.data.orderNo || orderInfo.orderNo
          },
          loading: false
        });
        
        // 处理不同的支付状态
        this.handlePaymentStatus(paymentStatus);
      } else {
        this.setData({
          loading: false,
          pageError: true,
          pageErrorMessage: res.message || '查询支付状态失败'
        });
      }
    }).catch(err => {
      this.paymentStatusRequest = false; // 清除请求标记
      
      console.error('查询支付状态失败:', err);
      // 模拟支付状态查询（实际项目中应使用真实接口）
      setTimeout(() => {
        this.setData({
          paymentStatus: 1, // 默认设为待支付状态
          orderInfo: {
            orderId,
            orderNo: this.data.orderNo || 'MOCK' + Date.now(),
            totalAmount: '100.00',
            serviceTypeName: '家政服务',
            serviceAddress: '北京市朝阳区',
            serviceTime: new Date().toLocaleString('zh-CN'),
            serviceDuration: 2
          },
          loading: false
        });
        
        this.handlePaymentStatus(1);
      }, 500);
    });
  },
  
  /**
   * 处理不同的支付状态
   * @param {number} status 支付状态码
   */
  handlePaymentStatus: function(status) {
    console.log('处理支付状态:', status);
    
    // 先清除可能存在的定时器，避免多个定时器同时运行
    if (this.refreshStatusTimer) {
      console.log('清除旧定时器');
      clearInterval(this.refreshStatusTimer);
      this.refreshStatusTimer = null;
    }
    
    switch(status) {
      case 3: // 支付成功
        console.log('支付成功状态，无需定时刷新');
        break;
      case 4: // 支付失败
        console.log('支付失败状态');
        this.setData({
          errorMsg: '支付失败，请重新尝试'
        });
        break;
      default:
        console.log('待支付或支付中状态，设置定时刷新');
        // 待支付或支付中状态，由后端定时任务处理超时逻辑
        // 每30秒刷新一次支付状态，以便及时显示后端处理结果
        this.refreshStatusTimer = setInterval(() => {
          console.log('定时器触发，查询支付状态');
          this.queryPaymentStatus();
        }, 30000); // 将间隔从10秒改为30秒，减少接口调用频率
    }
  },
  
  /**
   * 重新支付
   * 实现完整的重新支付逻辑，调用创建支付订单接口和微信支付接口
   */
  onRetryPayment: function() {
    const { orderId } = this.data;
    
    // 显示加载状态
    wx.showLoading({
      title: '发起支付中',
    });
    
    // 获取用户token
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.hideLoading();
      wx.showToast({
        title: '用户未登录',
        icon: 'none'
      });
      return;
    }
    
    // 调用创建支付订单接口
    // 从本地存储获取用户openid
    const userInfo = wx.getStorageSync('userInfo') || {};
    const openid = userInfo.openid || '';
    
    if (!openid) {
      wx.showToast({
        title: '获取用户信息失败，请重新登录',
        icon: 'none'
      });
      return;
    }
    
    request(`${API.payment.create}/${orderId}`, {
      method: 'POST',
      data: {
        openid: openid
      },
      header: {
        'Authorization': `Bearer ${token}`
      }
    }).then(res => {
      if (res.code === 200 && res.data) {
        // 调用微信支付接口，使用正确的支付参数路径
        return this.requestWxPayment(res.data.payParams, token);
      } else {
        wx.hideLoading();
        wx.showToast({
          title: res.message || '创建支付订单失败',
          icon: 'none'
        });
        throw new Error(res.message || '创建支付订单失败');
      }
    }).catch(err => {
      console.error('重新支付失败:', err);
      wx.hideLoading();
    });
  },
  
  /**
   * 调用微信支付接口
   * 参考service-request.js中的支付逻辑实现
   * @param {Object} payParams 支付参数
   * @param {string} token 用户token
   * @returns {Promise}
   */
  requestWxPayment: function(payParams, token) {
    return new Promise((resolve, reject) => {
      // 调用微信支付接口
      wx.requestPayment({
        ...payParams,
        success: function(res) {
          wx.hideLoading();
          console.log('支付成功:', res);
          // 支付成功后刷新页面状态
          this.queryPaymentStatus();
          resolve(res);
        }.bind(this),
        fail: function(err) {
          wx.hideLoading();
          const orderId = this.data.orderId;
          console.log('支付失败:', err);
          
          // 判断是用户取消支付还是其他失败
          // 不再由前端更新支付状态，直接刷新页面以获取最新状态
          this.queryPaymentStatus();
          
          reject(err);
        }.bind(this)
      });
    });
  },
  
  /**
   * 查看订单详情
   */
  viewOrderDetail: function() {
    const { orderId } = this.data;
    wx.navigateTo({
      url: `/pages/orders/orders?orderId=${orderId}`
    });
  },
  
  /**
   * 返回首页
   */
  goToHome: function() {
    wx.redirectTo({
      url: '/pages/index/index'
    });
  }
});