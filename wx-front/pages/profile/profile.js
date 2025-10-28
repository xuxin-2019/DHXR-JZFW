// 个人中心页面逻辑
const { API } = require('../../utils/api');
const { getServiceTypeName, ensureServiceTypesLoaded } = require('../../utils/util');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    userInfo: {},
    userRole: '',
    roleText: '',
    isLoading: true
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    // 检查是否已登录
    const token = wx.getStorageSync('token');
    if (!token) {
      // 未登录状态下，显示提示并返回首页
      wx.showToast({
        title: '请先登录',
        icon: 'none',
        duration: 2000,
        success: () => {
          setTimeout(() => {
            wx.navigateTo({
              url: '/pages/index/index'
            });
          }, 2000);
        }
      });
    } else {
      // 已登录状态下加载用户信息
      this.loadUserInfo();
    }
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 每次页面显示时重新加载用户信息，确保数据最新
    this.loadUserInfo();
  },

  /**
   * 加载用户信息
   */
  async loadUserInfo() {
    try {
      // 确保服务类型列表已加载
      await ensureServiceTypesLoaded();
      
      const userInfo = wx.getStorageSync('userInfo') || {};
      const userRole = wx.getStorageSync('userRole') || '';
      console.log('userRole:', userRole);
      console.log('userInfo:', userInfo);
      
      // 根据角色显示对应的文本
      let roleText = '';
      if (userRole === '1') {
        roleText = '用户';
      } else if (userRole === '2') {
        roleText = '护工';
        // 如果是护工，获取服务类型名称
        const serviceTypeName = getServiceTypeName(userInfo.serviceTypeId);
        // 创建一个新的用户信息对象，避免直接修改原始数据
        const updatedUserInfo = {
          ...userInfo,
          serviceTypeName: serviceTypeName
        };
        this.setData({
          userInfo: updatedUserInfo,
          userRole: userRole,
          roleText: roleText,
          isLoading: false
        });
        return;
      }
      
      this.setData({
        userInfo: userInfo,
        userRole: userRole,
        roleText: roleText,
        isLoading: false
      });
    } catch (error) {
      console.error('加载用户信息失败:', error);
      // 即使出错，也要继续显示用户信息
      const userInfo = wx.getStorageSync('userInfo') || {};
      const userRole = wx.getStorageSync('userRole') || '';
      let roleText = '';
      if (userRole === '1') {
        roleText = '用户';
      } else if (userRole === '2') {
        roleText = '护工';
      }
      
      this.setData({
        userInfo: userInfo,
        userRole: userRole,
        roleText: roleText,
        isLoading: false
      });
    }
  },

  /**
   * 跳转到修改信息页面
   */
  goToEditProfile() {
    wx.navigateTo({
      url: '../edit-profile/edit-profile'
    });
  },

  /**
   * 退出登录
   */
  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 调用登出接口
          wx.request({
            url: `${API.wx.login}/logout`,
            method: 'POST',
            header: {
              'content-type': 'application/json',
              'Authorization': `Bearer ${wx.getStorageSync('token')}`
            },
            success: (response) => {
              console.log('登出成功', response);
            },
            fail: (error) => {
              console.error('登出失败', error);
            },
            complete: () => {
              // 清除本地存储的用户信息
              wx.removeStorageSync('userInfo');
              wx.removeStorageSync('token');
              wx.removeStorageSync('userRole');
              wx.removeStorageSync('userId');
              
              // 跳转到首页
              wx.redirectTo({
                url: '../index/index'
              });
            }
          });
        }
      }
    });
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    this.loadUserInfo();
    wx.stopPullDownRefresh();
  },
  
  /**
   * 处理底部导航栏页面跳转
   * 实现自定义tabBar的页面切换功能，包括权限校验
   */
  navigateToPage(e) {
    const { page } = e.currentTarget.dataset;
    console.log('点击导航项，准备跳转到:', page);
    
    // 获取token和用户角色
    const token = wx.getStorageSync('token');
    const userRole = wx.getStorageSync('userRole');
    
    // 特殊处理订单页面的权限校验
    if (page === 'pages/orders/orders') {
      console.log('订单页面权限校验 - token:', !!token, 'role:', userRole);
      
      // 验证权限
      if (!token) {
        wx.showToast({
          title: '请先登录',
          icon: 'none'
        });
        return;
      }
      
      // 根据角色跳转到不同的订单页面
      if (userRole === '1') {
        // 用户角色跳转到用户订单页面
        wx.redirectTo({
          url: '/pages/orders/orders'
        });
      } else if (userRole === '2') {
        // 护工角色跳转到护工订单页面
        wx.redirectTo({
          url: '/pages/caregiver-orders/caregiver-orders'
        });
      } else {
        wx.showToast({
          title: '角色未知，无法访问订单页面',
          icon: 'none'
        });
      }
      return;
    }
    
    // 添加对"我的"页面的登录校验
    if (page === 'pages/profile/profile') {
      console.log('个人中心页面权限校验 - token:', !!token);
      
      // 验证是否已登录
      if (!token) {
        wx.showToast({
          title: '请先登录',
          icon: 'none'
        });
        return;
      }
    }
    
    // 统一使用redirectTo跳转，避免webview数量超限且解决非tabBar页面跳转问题
    wx.redirectTo({
      url: '/' + page,
      success: () => {
        console.log('成功跳转到:', page);
      },
      fail: (error) => {
        console.error('页面跳转失败:', error);
        wx.showToast({
          title: '页面跳转失败',
          icon: 'none'
        });
      }
    });
  }
});