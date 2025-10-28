// index.js

/**
 * 家政服务小程序首页
 * 展示服务类型、服务内容、用户评价及底部菜单
 */
Page({
  data: {
    // 是否可以使用getUserProfile API
    canIUseGetUserProfile: wx.canIUse('getUserProfile'),
    
    // 服务类型数据（模块1）
    serviceTypes: [
      { id: 1, name: '日常保洁', icon: '🧹', color: '#667eea' },
      { id: 2, name: '深度保洁', icon: '🛁', color: '#f093fb' },
      { id: 3, name: '照顾老人', icon: '👴', color: '#4facfe' },
      { id: 4, name: '照顾儿童', icon: '👶', color: '#fa709a' },
      { id: 5, name: '钟点工', icon: '⏰', color: '#43e97b' }
    ],
    
    // 服务内容详情（模块2）
    serviceDetails: [
      { 
        id: 1, 
        name: '日常保洁', 
        details: '室内清洁、厨房清洁、卫生间清洁、地板清洁', 
        price: '¥30/小时'
      },
      { 
        id: 2, 
        name: '深度保洁', 
        details: '全方位清洁、家具除尘、电器表面清洁、厨房油污清理', 
        price: '¥60/小时'
      },
      { 
        id: 3, 
        name: '照顾老人', 
        details: '生活照料、陪伴聊天、健康监测、协助用餐', 
        price: '¥50/小时'
      },
      { 
        id: 4, 
        name: '照顾儿童', 
        details: '接送上下学、日常照料、作业辅导、安全监护', 
        price: '¥55/小时'
      },
      { 
        id: 5, 
        name: '钟点工', 
        details: '灵活服务、按小时计费、可定制服务内容', 
        price: '¥35/小时'
      }
    ],
    
    // 用户评价数据（模块3，写死数据）
    reviews: [
      {
        id: 1,
        name: '张女士',
        avatar: '张',
        date: '2023-10-15',
        rating: [1, 2, 3, 4, 5],
        content: '阿姨很专业，打扫得非常干净，服务态度也很好，下次还会预约。'
      },
      {
        id: 2,
        name: '李先生',
        avatar: '李',
        date: '2023-10-10',
        rating: [1, 2, 3, 4, 5],
        content: '照顾老人很细心，有耐心，老人很喜欢，非常满意这次服务。'
      },
      {
        id: 3,
        name: '王女士',
        avatar: '王',
        date: '2023-10-05',
        rating: [1, 2, 3, 4],
        content: '钟点工服务很灵活，能够按照需求完成各项任务，性价比高。'
      }
    ],
    
    // 当前选中的服务类型ID
    selectedServiceId: null
  },
  
  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    // 页面加载时的初始化逻辑
    console.log('首页加载成功');
  },
  
  /**
   * 处理登录按钮点击事件
   * 实现完整的登录流程：获取用户信息 -> 获取code -> 跳转到信息填写页面
   */
  handleLogin() {
    // 检查是否可以使用getUserProfile API
    if (!this.data.canIUseGetUserProfile) {
      wx.showToast({
        title: '请使用较新版本的微信',
        icon: 'none'
      });
      return;
    }
    
    // 1. 首先直接调用getUserProfile获取用户信息（必须直接在点击事件中调用）
    wx.getUserProfile({
      desc: '用于登录和身份识别', // 声明获取用户个人信息后的用途
      success: (profileRes) => {
        console.log('获取用户信息成功:', profileRes);
        
        // 2. 在用户信息获取成功后，再调用wx.login获取code
        wx.login({
          success: (loginRes) => {
            if (loginRes.code) {
              console.log('获取登录code成功:', loginRes.code);
              
              // 3. 临时存储用户信息和code
              wx.setStorageSync('tempUserInfo', profileRes.userInfo);
              wx.setStorageSync('tempCode', loginRes.code);
              
              // 4. 跳转到登录信息填写页面 - 使用redirectTo避免webview数量超限
              wx.redirectTo({
                url: '../login/login'
              });
            } else {
              console.error('获取登录code失败:', loginRes);
              wx.showToast({
                title: '登录失败，请重试',
                icon: 'none'
              });
            }
          },
          fail: (loginError) => {
            console.error('获取登录code失败:', loginError);
            wx.showToast({
              title: '网络异常，请检查网络连接',
              icon: 'none'
            });
          }
        });
      },
      fail: (profileError) => {
        console.error('获取用户信息失败:', profileError);
        // 用户拒绝授权时可以给出提示
        if (profileError.errMsg.includes('auth deny')) {
          wx.showToast({
            title: '请授权用户信息以完成登录',
            icon: 'none'
          });
        }
      }
    });
  },
  
  /**
   * 选择服务类型
   * @param {Object} e - 事件对象，包含选中的服务类型ID
   */
  selectServiceType(e) {
    const serviceId = e.currentTarget.dataset.id;
    this.setData({
      selectedServiceId: serviceId
    });
    
    // 显示选中的服务信息
    const selectedService = this.data.serviceTypes.find(item => item.id === serviceId);
    
    // 获取用户角色
    const userRole = wx.getStorageSync('userRole');
    
    // 判断用户角色，只有用户角色(1)才能跳转服务申请页面
    if (userRole === '1') {
      // 准备服务ID映射（前端写死对应关系）
      const serviceIdMap = {
        1: '40001', // 日常保洁
        2: '40002', // 深度保洁
        3: '40003', // 照顾老人
        4: '40004', // 照顾儿童
        5: '40005'  // 钟点工
      };
      
      // 跳转到服务申请页面 - 使用redirectTo避免webview数量超限
      wx.redirectTo({
        url: `/pages/service-request/service-request?serviceId=${serviceIdMap[serviceId]}&serviceName=${selectedService.name}`
      });
    } else if (userRole === '2') {
      // 护工角色，不允许跳转
      wx.showToast({
        title: '护工角色不能申请服务',
        icon: 'none'
      });
    } else {
      // 未登录或角色未设置
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
    }
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
    
    // 执行页面跳转 - 使用redirectTo避免webview数量超限
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
