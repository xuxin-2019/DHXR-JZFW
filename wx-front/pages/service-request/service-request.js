// service-request.js

/**
 * 服务申请页面
 * 用于用户申请家政服务并创建订单
 */

// 导入API路由管理
import { API, request } from '../../utils/api';
Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 服务ID
    serviceId: '',
    // 服务名称
    serviceName: '',
    // 期望服务金额
    expectedPrice: '',
    // 期望服务时长（分钟）
    expectedDuration: '',
    // 服务地址
    serviceAddress: '',
    // 期望服务日期
    expectedDate: '',
    // 期望服务时间（时分）
    expectedTimeHourMinute: '',
    // 完整的期望服务时间（提交用）
    expectedTime: '',
    // 加载状态
    loading: false,
    // 当前日期（用于时间选择器的起始时间）
    currentDate: ''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    // 从URL参数获取服务ID和服务名称
    const { serviceId, serviceName } = options;
    
    this.setData({
      serviceId: serviceId,
      serviceName: decodeURIComponent(serviceName || '')
    });
    
    // 获取服务详情
    this.getServiceInfo();
  },

  /**
   * 获取服务详情
   */
  getServiceInfo: function() {
    this.setData({
      loading: true
    });
    
    // 初始化当前日期和时间
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    
    this.setData({
      currentDate: `${year}-${month}-${day}`,
      // 默认为当前时间
      expectedTimeHourMinute: `${hours}:${minutes}`,
      loading: false
    });
    
    // 不再获取服务详情，用户手动输入所有信息
  },

  /**
   * 监听地址输入变化
   */
  onAddressChange: function(e) {
    this.setData({
      serviceAddress: e.detail.value
    });
  },

  /**
   * 监听期望服务金额输入变化
   */
  onExpectedPriceChange: function(e) {
    this.setData({
      expectedPrice: e.detail.value
    });
  },

  /**
   * 监听期望服务时长输入变化
   */
  onExpectedDurationChange: function(e) {
    this.setData({
      expectedDuration: e.detail.value
    });
  },

  /**
   * 监听期望服务日期输入变化
   */
  onExpectedDateChange: function(e) {
    const date = e.detail.value;
    this.setData({
      expectedDate: date
    });
    // 更新完整的期望服务时间
    this.updateExpectedTime();
  },

  /**
   * 监听期望服务时间（时分）输入变化
   */
  onExpectedTimeChange: function(e) {
    const time = e.detail.value;
    this.setData({
      expectedTimeHourMinute: time
    });
    // 更新完整的期望服务时间
    this.updateExpectedTime();
  },

  /**
   * 更新完整的期望服务时间
   */
  updateExpectedTime: function() {
    const { expectedDate, expectedTimeHourMinute } = this.data;
    if (expectedDate && expectedTimeHourMinute) {
      this.setData({
        expectedTime: `${expectedDate} ${expectedTimeHourMinute}`
      });
    }
  },

  /**
   * 表单验证
   */
  validateForm: function() {
    const { serviceAddress, expectedPrice, expectedDuration, expectedDate, expectedTimeHourMinute, expectedTime } = this.data;
    
    // 验证地址
    if (!serviceAddress.trim()) {
      wx.showToast({
        title: '请输入服务地址',
        icon: 'none'
      });
      return false;
    }
    
    // 验证期望服务金额
    if (!expectedPrice || isNaN(expectedPrice) || parseFloat(expectedPrice) <= 0) {
      wx.showToast({
        title: '请输入有效的期望服务金额',
        icon: 'none'
      });
      return false;
    }
    
    // 验证期望服务时长
    if (!expectedDuration || isNaN(expectedDuration) || parseInt(expectedDuration) <= 0) {
      wx.showToast({
        title: '请输入有效的期望服务时长',
        icon: 'none'
      });
      return false;
    }
    
    // 验证期望服务日期
    if (!expectedDate) {
      wx.showToast({
        title: '请选择期望服务日期',
        icon: 'none'
      });
      return false;
    }
    
    // 验证期望服务时间（时分）
    if (!expectedTimeHourMinute) {
      wx.showToast({
        title: '请选择期望服务时间',
        icon: 'none'
      });
      return false;
    }
    
    // 确保完整时间已更新
    this.updateExpectedTime();
    
    // 验证期望服务时间是否晚于当前时间
    const expectedDateTime = new Date(expectedTime.replace(' ', 'T'));
    const now = new Date();
    if (expectedDateTime <= now) {
      wx.showToast({
        title: '期望服务时间必须晚于当前时间',
        icon: 'none'
      });
      return false;
    }
    
    return true;
  },

  /**
   * 提交服务申请
   */
  submitRequest: function() {
    // 表单验证
    if (!this.validateForm()) {
      return;
    }
    
    const { serviceId, serviceName, serviceAddress, expectedTime, expectedPrice, expectedDuration } = this.data;
    
    // 获取token
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }
    
    this.setData({
      loading: true
    });
    
    // 构造请求数据
    const requestData = {
      userId: wx.getStorageSync('userId'),
      serviceTypeId: serviceId,
      totalAmount: parseFloat(expectedPrice),
      serviceAddress: serviceAddress,
      serviceTime: expectedTime + ':00', // 期望服务时间，后台需要秒，添加:00
      serviceDuration: parseInt(expectedDuration) // 服务时长（单位：分钟）
    };
    
    console.log('提交的订单数据:', requestData);
    
    // 调用创建订单接口
    request(API.order.create, {
      method: 'POST',
      data: requestData,
      header: {
        'Authorization': `Bearer ${token}`
      }
    }).then(res => {
      console.log('创建订单成功:', res);
      
      if (res.code === 200 || res.success) {
        // 订单创建成功
        wx.showToast({
          title: '订单创建成功',
          icon: 'success'
        });
        
        // 延迟跳转到首页
        setTimeout(() => {
          wx.redirectTo({
            url: '../index/index'
          });
        }, 1500);
      } else {
        wx.showToast({
          title: res.message || '创建订单失败',
          icon: 'none'
        });
      }
    }).catch(err => {
        console.error('创建订单失败:', err);
        wx.showToast({
          title: '网络异常，请稍后重试',
          icon: 'none'
        });
    }).finally(() => {
      this.setData({
        loading: false
      });
    });
  }
});