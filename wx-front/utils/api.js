// api.js
// 统一管理后端API路由

// 后端服务基础地址
const BASE_URL = 'http://127.0.0.1:8080/homemaker/api';
// const BASE_URL = 'http://120.53.84.167/homemaker/api';

// API路由配置
export const API = {
  // 便捷登录接口
  login: `${BASE_URL}/wx/login`,
  
  // 微信登录相关接口
  wx: {
    // 登录接口
    login: `${BASE_URL}/wx/login`
  },
  // 服务类型相关接口
  serviceType: {
    // 获取服务类型列表
    list: `${BASE_URL}/service-type/list`,
    // 获取服务详情
    info: `${BASE_URL}/service-type/info`
  },
  // 可在此添加更多模块的API路由
  user: {
    // 创建用户
    create: `${BASE_URL}/user/create`,
    // 根据ID获取用户信息
    getById: `${BASE_URL}/user/getById`,
    // 根据openid获取用户信息
    getByOpenid: `${BASE_URL}/user/getByOpenid`,
    // 更新用户信息
    update: `${BASE_URL}/user/update`
  },
  nurse: {
    // 更新护工信息
    update: `${BASE_URL}/nurse/update`
  },
  order: {
    // 创建订单
    create: `${BASE_URL}/order/create`,
    // 获取订单列表
    list: `${BASE_URL}/order/list`,
    // 微信小程序端查询订单列表（支持userId和status数组）
    wxList: `${BASE_URL}/order/wxList`,
    // 更新订单状态
    updateStatus: `${BASE_URL}/order/status`
  },
  caregiver: {
    // 护工相关接口预留
  },
  caregiverOrder: {
    // 获取护工订单列表
    list: `${BASE_URL}/order/nurseList`
  },
  evaluation: {
    // 创建评价
    create: `${BASE_URL}/evaluation/create`
  }
};

// 请求方法封装
export const request = (url, options = {}) => {
  // 从本地存储获取token
  const token = wx.getStorageSync('token') || '';
  
  return new Promise((resolve, reject) => {
    wx.request({
      url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'content-type': 'application/json',
        // 如果有token，添加到请求头
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
        ...options.header
      },
      success: (res) => {
        // 检查响应是否包含data字段
        if (res && res.data) {
          // 处理未登录或token过期的情况
          if (res.data.code === 401 || (res.data.message && res.data.message.includes('登录'))) {
            // 清除过期的token
            wx.removeStorageSync('token');
            wx.removeStorageSync('userInfo');
            wx.removeStorageSync('userRole');
            
            // 提示用户重新登录
            wx.showToast({
              title: '请重新登录',
              icon: 'none'
            });
            
            // 跳转到登录页
            setTimeout(() => {
              wx.navigateTo({
                url: '/pages/login/login'
              });
            }, 1500);
          }
          
          // 正确返回响应数据
          resolve(res.data);
        } else {
          // 响应格式不正确
          resolve({ code: -1, message: '响应格式错误' });
        }
      },
      fail: (err) => {
        reject(err);
      },
      complete: options.complete
    });
  });
};