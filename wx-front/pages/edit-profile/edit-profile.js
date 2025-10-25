// 修改个人信息页面逻辑
const { API } = require('../../utils/api');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    userInfo: {},
    userRole: '',
    roleText: '',
    isSubmitting: false,
    // 表单数据
    formData: {
      name: '',
      phone: '',
      address: '',
      age: '',
      serviceTypeId: ''
    },
    // 服务类型列表（护工角色使用）
    serviceTypes: []
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.loadUserInfo();
  },

  /**
   * 获取服务类型名称
   */
  getServiceTypeName(serviceTypeId) {
    const { serviceTypes } = this.data;
    for (let i = 0; i < serviceTypes.length; i++) {
      if (serviceTypes[i].id === serviceTypeId) {
        return serviceTypes[i].name;
      }
    }
    return '';
  },

  /**
   * 加载用户信息
   */
  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    const userRole = wx.getStorageSync('userRole') || '';
    
    // 根据角色显示对应的文本
    let roleText = '';
    if (userRole === '1') {
      roleText = '用户';
    } else if (userRole === '2') {
      roleText = '护工';
      this.loadServiceTypes();
    }
    
    // 初始化表单数据
    const formData = {
      name: userInfo.name || '',
      phone: userInfo.phone || '',
      address: userInfo.address || '',
      age: userInfo.age || '',
      serviceTypeId: userInfo.serviceTypeId || ''
    };
    
    this.setData({
      userInfo: userInfo,
      userRole: userRole,
      roleText: roleText,
      formData: formData
    });
  },

  /**
   * 加载服务类型列表（护工角色使用）
   */
  loadServiceTypes() {
    wx.request({
      url: API.serviceType.list,
      method: 'GET',
      header: {
        'content-type': 'application/json',
        'Authorization': `Bearer ${wx.getStorageSync('token')}`
      },
      success: (res) => {
        if (res.data && res.data.code === 200) {
          this.setData({
            serviceTypes: res.data.data || []
          });
        }
      },
      fail: (error) => {
        console.error('获取服务类型失败', error);
      }
    });
  },

  /**
   * 表单输入事件处理
   */
  onInput(e) {
    const { field } = e.currentTarget.dataset;
    const { value } = e.detail;
    
    this.setData({
      [`formData.${field}`]: value
    });
  },

  /**
   * 表单提交
   */
  submitForm() {
    // 表单验证
    if (!this.validateForm()) {
      return;
    }
    
    this.setData({ isSubmitting: true });
    
    const userId = wx.getStorageSync('userId');
    const token = wx.getStorageSync('token');
    const { formData, userRole } = this.data;
    
    // 根据角色选择不同的API
    let apiUrl = '';
    let requestData = {};
    
    if (userRole === '1') {
      // 用户角色
      apiUrl = API.user.update;
      requestData = {
        id: userId,
        name: formData.name,
        phone: formData.phone,
        address: formData.address
      };
    } else if (userRole === '2') {
      // 护工角色
      apiUrl = API.nurse.update;
      requestData = {
        id: userId,
        name: formData.name,
        phone: formData.phone,
        age: formData.age,
        serviceTypeId: formData.serviceTypeId
      };
    }
    
    // 发送请求
    wx.request({
      url: apiUrl,
      method: 'POST',
      data: requestData,
      header: {
        'content-type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        if (res.data && res.data.code === 200) {
          wx.showToast({
            title: '修改成功',
            icon: 'success',
            duration: 2000
          });
          
          // 更新本地存储的用户信息
          const updatedUserInfo = { ...this.data.userInfo, ...formData };
          wx.setStorageSync('userInfo', updatedUserInfo);
          
          // 返回上一页
          setTimeout(() => {
            wx.navigateBack();
          }, 2000);
        } else {
          wx.showToast({
            title: res.data?.message || '修改失败',
            icon: 'none'
          });
        }
      },
      fail: (error) => {
        console.error('修改信息失败', error);
        wx.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        });
      },
      complete: () => {
        this.setData({ isSubmitting: false });
      }
    });
  },

  /**
   * 表单验证
   */
  validateForm() {
    const { formData, userRole } = this.data;
    
    // 通用验证：姓名和电话
    if (!formData.name.trim()) {
      wx.showToast({
        title: '请输入姓名',
        icon: 'none'
      });
      return false;
    }
    
    if (!formData.phone.trim()) {
      wx.showToast({
        title: '请输入电话',
        icon: 'none'
      });
      return false;
    }
    
    // 手机号格式验证
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(formData.phone)) {
      wx.showToast({
        title: '请输入正确的手机号',
        icon: 'none'
      });
      return false;
    }
    
    // 用户角色特有验证
    if (userRole === '1' && !formData.address.trim()) {
      wx.showToast({
        title: '请输入地址',
        icon: 'none'
      });
      return false;
    }
    
    // 护工角色特有验证
    if (userRole === '2') {
      if (!formData.age) {
        wx.showToast({
          title: '请输入年龄',
          icon: 'none'
        });
        return false;
      }
      
      if (isNaN(formData.age) || formData.age < 18 || formData.age > 80) {
        wx.showToast({
          title: '请输入18-80之间的年龄',
          icon: 'none'
        });
        return false;
      }
      
      if (!formData.serviceTypeId) {
        wx.showToast({
          title: '请选择服务类型',
          icon: 'none'
        });
        return false;
      }
    }
    
    return true;
  }
});