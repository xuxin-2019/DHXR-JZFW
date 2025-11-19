// payment-result.js

/**
 * 支付结果页面
 * 展示支付成功或失败的结果
 */
Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 支付状态：success 成功，fail 失败
    status: 'fail',
    // 订单ID
    orderId: '',
    // 订单号
    orderNo: '',
    // 订单信息
    orderInfo: null,
    // 加载状态
    loading: true
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    // 获取页面参数
    const status = options.status || 'fail';
    const orderId = options.orderId || '';
    const orderNo = options.orderNo || '';
    
    this.setData({ status, orderId, orderNo });
    
    // 如果有订单ID，可以获取订单详情（可选）
    if (orderId) {
      this.getOrderDetail();
    } else {
      this.setData({ loading: false });
    }
  },

  /**
   * 获取订单详情
   */
  getOrderDetail: function() {
    // 这里可以根据需求调用获取订单详情的接口
    // 由于接口可能未提供，这里模拟数据
    setTimeout(() => {
      this.setData({
        orderInfo: {
          orderNo: this.data.orderNo,
          orderAmount: '100.00',
          createTime: new Date().toLocaleString('zh-CN')
        },
        loading: false
      });
    }, 500);
  },

  /**
   * 查看订单详情
   */
  viewOrderDetail: function() {
    wx.navigateTo({
      url: `/pages/order-detail/order-detail?orderId=${this.data.orderId}`
    });
  },

  /**
   * 返回首页
   */
  goToHome: function() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },

  /**
   * 重新支付
   */
  retryPayment: function() {
    wx.navigateBack();
  }
});