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
    // 服务价格
    servicePrice: 0,
    // 服务时长
    serviceDuration: 0,
    // 服务地址
    serviceAddress: '',
    // 服务开始日期
    startDate: '',
    // 服务开始时间（时分）
    startTimeHourMinute: '',
    // 服务结束日期
    endDate: '',
    // 服务结束时间（时分）
    endTimeHourMinute: '',
    // 完整的开始时间（提交用）
    startTime: '',
    // 完整的结束时间（提交用）
    endTime: '',
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
    const { serviceId } = this.data;
    
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
      startTimeHourMinute: `${hours}:${minutes}`,
      endTimeHourMinute: `${hours}:${minutes}`
    });
    
    request(`${API.serviceType.info}?id=${serviceId}`, {
      method: 'GET'
    }).then(res => {
      console.log('获取服务详情成功:', res.data);
      
      if (res.data.code === 200 && res.data.data) {
        // 更新服务信息
        this.setData({
          servicePrice: res.data.data.price || 0,
          serviceDuration: res.data.data.duration || 0
        });
      } else {
        wx.showToast({
          title: res.data.message || '获取服务信息失败',
          icon: 'none'
        });
      }
    }).catch(err => {
      console.error('获取服务详情失败:', err);
      wx.showToast({
        title: '网络异常，请检查网络连接',
        icon: 'none'
      });
    }).finally(() => {
      this.setData({
        loading: false
      });
    });
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
   * 监听开始日期输入变化
   */
  onStartDateChange: function(e) {
    const date = e.detail.value;
    this.setData({
      startDate: date
    });
    // 更新完整的开始时间
    this.updateStartTime();
  },

  /**
   * 监听开始时间（时分）输入变化
   */
  onStartTimeChange: function(e) {
    const time = e.detail.value;
    this.setData({
      startTimeHourMinute: time
    });
    // 更新完整的开始时间
    this.updateStartTime();
  },

  /**
   * 监听结束日期输入变化
   */
  onEndDateChange: function(e) {
    const date = e.detail.value;
    this.setData({
      endDate: date
    });
    // 更新完整的结束时间
    this.updateEndTime();
  },

  /**
   * 监听结束时间（时分）输入变化
   */
  onEndTimeChange: function(e) {
    const time = e.detail.value;
    this.setData({
      endTimeHourMinute: time
    });
    // 更新完整的结束时间
    this.updateEndTime();
  },

  /**
   * 更新完整的开始时间
   */
  updateStartTime: function() {
    const { startDate, startTimeHourMinute } = this.data;
    if (startDate && startTimeHourMinute) {
      this.setData({
        startTime: `${startDate} ${startTimeHourMinute}`
      });
    }
  },

  /**
   * 更新完整的结束时间
   */
  updateEndTime: function() {
    const { endDate, endTimeHourMinute } = this.data;
    if (endDate && endTimeHourMinute) {
      this.setData({
        endTime: `${endDate} ${endTimeHourMinute}`
      });
    }
  },

  /**
   * 表单验证
   */
  validateForm: function() {
    const { serviceAddress, startDate, startTimeHourMinute, endDate, endTimeHourMinute, startTime, endTime } = this.data;
    
    // 验证地址
    if (!serviceAddress.trim()) {
      wx.showToast({
        title: '请输入服务地址',
        icon: 'none'
      });
      return false;
    }
    
    // 验证开始日期
    if (!startDate) {
      wx.showToast({
        title: '请选择服务开始日期',
        icon: 'none'
      });
      return false;
    }
    
    // 验证开始时间（时分）
    if (!startTimeHourMinute) {
      wx.showToast({
        title: '请选择服务开始时间',
        icon: 'none'
      });
      return false;
    }
    
    // 验证结束日期
    if (!endDate) {
      wx.showToast({
        title: '请选择服务结束日期',
        icon: 'none'
      });
      return false;
    }
    
    // 验证结束时间（时分）
    if (!endTimeHourMinute) {
      wx.showToast({
        title: '请选择服务结束时间',
        icon: 'none'
      });
      return false;
    }
    
    // 确保完整时间已更新
    this.updateStartTime();
    this.updateEndTime();
    
    // 验证结束时间是否晚于开始时间
    const startDateTime = new Date(startTime.replace(' ', 'T'));
    const endDateTime = new Date(endTime.replace(' ', 'T'));
    if (endDateTime <= startDateTime) {
      wx.showToast({
        title: '结束时间必须晚于开始时间',
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
    
    const { serviceId, serviceName, serviceAddress, startTime, endTime, servicePrice } = this.data;
    
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
      totalAmount: servicePrice,
      serviceAddress: serviceAddress,
      startTime: startTime + ':00', // 后台需要秒，添加:00
      endTime: endTime + ':00' // 后台需要秒，添加:00
    };
    
    // 调用创建订单接口
    request(API.order.create, {
      method: 'POST',
      data: requestData,
      header: {
        'Authorization': `Bearer ${token}`
      }
    }).then(res => {
      console.log('创建订单成功:', res.data);
      
      if (res.data.code === 200 || res.data.success) {
        // 订单创建成功
        wx.showToast({
          title: '订单创建成功',
          icon: 'success'
        });
        
        // 延迟跳转到首页
        setTimeout(() => {
          wx.switchTab({
            url: '../index/index'
          });
        }, 1500);
      } else {
        wx.showToast({
          title: res.data.message || '创建订单失败',
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