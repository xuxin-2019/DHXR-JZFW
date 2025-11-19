// login.js

/**
 * 登录信息页面
 * 用于完善用户登录信息并提交到后端
 */

// 导入API路由管理
import { API } from '../../utils/api';
Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 从首页传递过来的用户信息
    userInfo: {},
    // 微信临时登录凭证code
    code: '',
    // 用户角色（1:用户，2:护工）
    role: '1',
    // 手机号码
    phone: '',
    // 年龄（护工必填）
    age: '',
    // 家庭地址（用户必填）
    address: '',
    // 服务类型ID（护工必填）
    serviceTypeId: '',
    // 选中的服务类型名称
    serviceTypeName: '',
    // 选中的服务类型索引
    serviceTypeIndex: 0,
    // 服务类型选项列表（从后端获取）
    serviceTypes: [],
    // 是否可以提交（表单验证通过）
    canSubmit: false,
    // 错误信息
    errorMsg: '',
    // 是否正在加载服务类型
    loadingServiceTypes: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    // 从首页获取用户信息和code
    const userInfo = wx.getStorageSync('tempUserInfo') || {};
    const code = wx.getStorageSync('tempCode') || '';
    
    if (!userInfo.nickName || !code) {
      // 如果没有用户信息或code，返回首页
      wx.showToast({
        title: '请先点击登录',
        icon: 'none'
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }
    
    this.setData({
      userInfo: userInfo,
      code: code
    });
    
    // 加载服务类型列表
    this.loadServiceTypes();
  },
  
  /**
   * 从后端加载服务类型列表
   */
  loadServiceTypes() {
    this.setData({
      loadingServiceTypes: true
    });
    
    wx.request({
      url: API.serviceType.list,
      method: 'GET',
      success: (res) => {
        console.log('获取服务类型成功:', res.data);
        
        if (res.data.code === 200 && res.data.data && res.data.data.length > 0) {
          // 将服务类型列表存储到本地
          wx.setStorageSync('serviceTypes', res.data.data);
          
          this.setData({
            serviceTypes: res.data.data,
            // 默认选中第一个服务类型
            serviceTypeIndex: 0,
            serviceTypeId: res.data.data[0].id,
            serviceTypeName: res.data.data[0].name
          });
          // 验证表单
          this.validateForm();
        } else {
          console.error('获取服务类型失败:', res.data.message || '未知错误');
          this.setData({
            errorMsg: '获取服务类型失败，请稍后重试'
          });
        }
      },
      fail: (err) => {
        console.error('获取服务类型请求失败:', err);
        this.setData({
          errorMsg: '网络异常，请检查网络连接'
        });
      },
      complete: () => {
        this.setData({
          loadingServiceTypes: false
        });
      }
    });
  },

  /**
   * 监听角色选择变化
   */
  onRoleChange(e) {
    this.setData({
      role: e.detail.value
    });
    this.validateForm();
  },

  /**
   * 监听手机号码输入变化
   */
  onPhoneChange(e) {
    this.setData({
      phone: e.detail.value
    });
    this.validateForm();
  },

  /**
   * 监听年龄输入变化
   */
  onAgeChange(e) {
    this.setData({
      age: e.detail.value
    });
    this.validateForm();
  },

  /**
   * 监听地址输入变化
   */
  onAddressChange(e) {
    this.setData({
      address: e.detail.value
    });
    this.validateForm();
  },

  /**
   * 监听服务类型选择变化
   */
  onServiceTypeChange(e) {
    const index = e.detail.value;
    const selectedService = this.data.serviceTypes[index];
    
    this.setData({
      serviceTypeIndex: index,
      serviceTypeId: selectedService.id,
      serviceTypeName: selectedService.name
    });
    
    this.validateForm();
  },

  /**
   * 根据角色判断显示的字段
   */
  isUser() {
    return this.data.role === '1';
  },

  /**
   * 根据角色判断显示的字段
   */
  isCaregiver() {
    return this.data.role === '2';
  },

  /**
   * 表单验证
   * 根据不同角色验证不同的必填字段
   */
  validateForm: function() {
    // 验证手机号格式（11位数字）- 所有角色都需要
    const phoneRegex = /^1[3-9]\d{9}$/;
    const isPhoneValid = phoneRegex.test(this.data.phone);
    
    let isValid = isPhoneValid;
    
    if (this.data.role === '1') { // 用户角色
      // 用户只需要验证手机号和家庭地址
      const isAddressValid = this.data.address.trim().length > 0;
      isValid = isPhoneValid && isAddressValid;
    } else if (this.data.role === '2') { // 护工角色
      // 护工需要验证手机号、年龄和服务类型
      const ageNum = parseInt(this.data.age);
      const isAgeValid = !isNaN(ageNum) && ageNum >= 18 && ageNum <= 65;
      const isServiceTypeValid = this.data.serviceTypeId !== '';
      isValid = isPhoneValid && isAgeValid && isServiceTypeValid;
    }
    
    this.setData({
      canSubmit: isValid
    });
  },

  /**
   * 提交登录信息到后端
   */
  onSubmit() {
    const { userInfo, code, role, phone, age, address } = this.data;
    
    // 显示加载提示
    wx.showLoading({
      title: '登录中...',
    });
    
    // 根据角色封装不同的请求数据
    const requestData = {
      code: code,
      userInfo: userInfo,
      role: role,
      phone: phone
    };
    
    // 根据不同角色添加特定字段
    if (role === '1') { // 用户角色
      requestData.address = address;
    } else if (role === '2') { // 护工角色
      requestData.age = parseInt(age);
      requestData.serviceTypeId = this.data.serviceTypeId; // 提交服务ID
    }
    
    // 调用后端登录接口
    wx.request({
      url: API.wx.login,
      method: 'POST',
      data: requestData,
      success: (res) => {
        console.log('登录中！！');
        
        if (res.data.code === 200 || res.data.success) {
          // 登录成功，保存用户信息到本地存储
          wx.setStorageSync('userInfo', res.data.data?.userInfo || {});
          wx.setStorageSync('token', res.data.data?.token || '');
          wx.setStorageSync('openid', res.data.data?.openid || '');
          // 存储用户角色标识到本地
          wx.setStorageSync('userRole', role);
          wx.setStorageSync('userId', res.data.data?.id || '');

          // 清除临时数据
          wx.removeStorageSync('tempUserInfo');
          wx.removeStorageSync('tempCode');

          // 显示成功提示
          wx.showToast({
            title: '登录成功',
            icon: 'success'
          });

          // 跳转到首页
          setTimeout(() => {
            wx.redirectTo({
              url: '../index/index',
              success: function() {
                console.log('跳转成功');
              },
              fail: function(err) {
                console.error('跳转失败:', err);
              }
            });
          }, 1500);
        } else {
          // 登录失败
          this.setData({
            errorMsg: res.data.message || '登录失败，请重试'
          });
        }
      },
      fail: (err) => {
        console.error('登录请求失败:', err);
        this.setData({
          errorMsg: '网络异常，请检查网络连接'
        });
      },
      complete: () => {
        // 隐藏加载提示
        wx.hideLoading();
      }
    });
  }
});