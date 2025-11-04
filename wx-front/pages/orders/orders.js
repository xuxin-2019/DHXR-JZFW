// orders.js

/**
 * 用户订单页面
 * 展示用户的服务订单列表，支持按状态筛选
 */

// 导入API路由管理
import { API, request } from '../../utils/api';
Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 订单列表数据
    orderList: [],
    // 加载状态
    loading: false,
    // 刷新状态
    refreshing: false,
    // 当前选中的tab索引
    currentTab: 0,
    // tab栏数据
    tabs: [
      { id: 'all', name: '全部', statusList: [] },
      { id: 'pending', name: '派单中', statusList: [1, 2] },
      { id: 'accepted', name: '已接单', statusList: [3] },
      { id: 'completed', name: '已完成', statusList: [5] }
    ],
    // 用户信息
    userInfo: wx.getStorageSync('userInfo') || {},
    token: wx.getStorageSync('token') || '',
    role: wx.getStorageSync('userRole') || '',
    userId: wx.getStorageSync('userId') || '',
    // 分页相关
    page: 1,
    pageSize: 10,
    hasMore: true,
    loadingMore: false,
    // 评价相关数据
    showReviewModal: false,
    currentOrderId: '',
    currentNurseId: '',
    currentRating: 5,
    reviewContent: ''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    // 权限验证已在app.js的tab切换时完成，此处不再重复验证
    // 直接加载订单列表
    this.loadOrderList();
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function() {
    // 每次显示页面时重新加载订单列表
    if (this.data.userInfo.role === '1') {
      this.loadOrderList();
    }
  },
  

  /**
   * 加载订单列表
   * @param {boolean} refresh 是否为刷新操作
   */
  loadOrderList: function(refresh = false) {
    // 如果是刷新操作，重置分页状态
    if (refresh) {
      this.setData({ 
        refreshing: true,
        page: 1,
        hasMore: true 
      });
    } else {
      // 如果是初次加载或切换tab
      if (this.data.page === 1) {
        this.setData({ loading: true });
      }
    }
    
    // 获取当前选中的tab
    const currentTab = this.data.currentTab;
    const selectedTab = this.data.tabs[currentTab];
    
    // 构建请求参数
    const params = {
      userId: this.data.userId,
      page: this.data.page,
      pageSize: this.data.pageSize
    };
    
    // 如果不是全部订单，添加状态数组参数
    if (selectedTab.statusList && selectedTab.statusList.length > 0) {
      params.status = selectedTab.statusList; // 直接传递数组
    }
    
    // 调用新的API获取订单列表
    request(API.order.wxList, {
      method: 'GET',
      data: params
    }).then(res => {
      // 直接使用res，因为request现在返回的是res.data
      if (res.code === 200 && res.data) {
        let newOrderList = [];
        
        // 格式化订单数据
        if (res.data.records && Array.isArray(res.data.records)) {
          newOrderList = this.formatOrderData(res.data.records);
        } else {
          // 兼容旧格式
          newOrderList = this.formatOrderData(res.data);
        }
        
        // 根据操作类型设置订单列表
        if (refresh || this.data.page === 1) {
          this.setData({
            orderList: newOrderList
          });
        } else {
          // 上拉加载时追加数据
          this.setData({
            orderList: [...this.data.orderList, ...newOrderList]
          });
        }
        
        // 判断是否还有更多数据
        const total = res.data.total || 0;
        const hasMore = this.data.orderList.length < total;
        this.setData({ hasMore });
        
      } else {
        // 使用Toast提示错误信息
        wx.showToast({
          title: res.message || '获取订单列表失败',
          icon: 'none'
        });
        // 只有在初始加载或刷新时才清空订单列表
        if (refresh || this.data.page === 1) {
          this.setData({ orderList: [] });
        }
      }
    }).catch(err => {
      console.error('获取订单列表失败:', err);
      // 使用Toast提示错误信息
      wx.showToast({
        title: '网络异常，请检查网络连接',
        icon: 'none'
      });
      // 只有在初始加载或刷新时才清空订单列表
      if (refresh || this.data.page === 1) {
        this.setData({ orderList: [] });
      }
    }).finally(() => {
      // 重置加载状态
      this.setData({ 
        loading: false,
        refreshing: false,
        loadingMore: false
      });
    });
  },

  /**
   * 格式化订单数据
   * @param {Array} orders 原始订单数据
   * @returns {Array} 格式化后的订单数据
   */
  formatOrderData: function(orders) {
    // 订单状态映射
    const statusMap = {
      1: { text: '待派单', color: '#ff9800' },
      2: { text: '匹配中', color: '#2196f3' },
      3: { text: '已接单', color: '#4caf50' },
      4: { text: '服务中', color: '#00bcd4' },
      5: { text: '已完成', color: '#8bc34a' },
      6: { text: '已取消', color: '#9e9e9e' },
      7: { text: '已拒绝', color: '#e91e63' }
    };
    
    // 过滤掉已取消和已拒绝的订单
    const filteredOrders = orders.filter(order => {
      return order.status !== 6 && order.status !== 7;
    });
    
    return filteredOrders.map(order => {
      // 确定服务时间（优先使用新字段，兼容旧数据）
      let serviceTimeFormatted = '';
      if (order.serviceTime) {
        serviceTimeFormatted = this.formatDateTime(order.serviceTime);
      } else if (order.startTime) {
        serviceTimeFormatted = this.formatDateTime(order.startTime);
      }
      
      return {
        ...order,
        // 映射服务类型和金额字段，确保与WXML模板匹配
        serviceName: order.serviceTypeName || order.serviceType || '',
        amount: order.totalAmount || order.price || 0,
        // 服务时间
        serviceTimeFormatted: serviceTimeFormatted,
        // 服务时长（单位：分钟）
        serviceDurationFormatted: order.serviceDuration ? `${order.serviceDuration}分钟` : '',
        // 获取状态文本和颜色
        statusInfo: statusMap[order.status] || { text: '未知状态', color: '#9e9e9e' }
      };
    });
  },

  /**
   * 格式化日期时间
   * @param {string} dateTime 日期时间字符串
   * @returns {string} 格式化后的日期时间
   */
  formatDateTime: function(dateTime) {
    if (!dateTime) return '';
    
    const date = new Date(dateTime);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  },

  /**
   * 处理tab切换
   * @param {Object} e 事件对象
   */
  /**
   * 处理评价按钮点击事件
   * @param {Object} e 事件对象
   */
  onReviewButtonTap: function(e) {
    const orderId = e.currentTarget.dataset.id;
    const nurseId = e.currentTarget.dataset.nurseid;
    
    this.setData({
      showReviewModal: true,
      currentOrderId: orderId,
      currentNurseId: nurseId,
      currentRating: 5,
      reviewContent: ''
    });
  },
  
  /**
   * 关闭评价弹窗
   */
  closeReviewModal: function() {
    this.setData({
      showReviewModal: false
    });
  },
  
  /**
   * 选择评分
   * @param {Object} e 事件对象
   */
  selectRating: function(e) {
    const rating = e.currentTarget.dataset.rating;
    this.setData({
      currentRating: rating
    });
  },
  
  /**
   * 处理评价内容输入
   * @param {Object} e 事件对象
   */
  onReviewInput: function(e) {
    this.setData({
      reviewContent: e.detail.value
    });
  },
  
  /**
   * 提交评价
   */
  submitReview: function() {
    const evaluation = {
      orderId: this.data.currentOrderId,
      nurseId: this.data.currentNurseId,
      userId: this.data.userId,
      rating: this.data.currentRating,
      content: this.data.reviewContent
    };
    
    wx.showLoading({ title: '提交中...' });
    
    // 调用API创建评价
    request(API.evaluation.create, {
      method: 'POST',
      data: evaluation
    }).then(res => {
      wx.hideLoading();
      
      if (res.code === 200) {
        wx.showToast({ title: '评价成功' });
        this.closeReviewModal();
        // 重新加载订单列表，更新订单状态
        this.loadOrderList(true);
      } else {
        wx.showToast({ 
          title: res.message || '评价失败', 
          icon: 'none' 
        });
      }
    }).catch(error => {
      wx.hideLoading();
      wx.showToast({ 
        title: '网络错误，请重试', 
        icon: 'none' 
      });
      console.error('提交评价失败:', error);
    });
  },
  
  /**
   * 处理tab切换
   * @param {Object} e 事件对象
   */
  onTabChange: function(e) {
    const index = e.currentTarget.dataset.index;
    
    this.setData({
      currentTab: index,
      page: 1, // 切换tab时重置分页
      hasMore: true
    });
    
    // 切换tab后重新加载订单列表
    this.loadOrderList();
  },

  /**
   * 处理下拉刷新
   */
  onPullDownRefresh: function() {
    this.loadOrderList(true);
  },
  
  /**
   * 处理上拉加载更多
   * 实现下滑加载功能
   */
  onReachBottom: function() {
    // 如果已经没有更多数据或正在加载中，则不执行操作
    if (!this.data.hasMore || this.data.loadingMore || this.data.loading) {
      return;
    }
    
    // 设置加载更多状态
    this.setData({
      loadingMore: true,
      page: this.data.page + 1 // 增加页码
    });
    
    // 加载下一页数据
    this.loadOrderList();
  },

  /**
   * 处理订单卡片点击
   * @param {Object} e 事件对象
   */
  onOrderCardTap: function(e) {
    const orderId = e.currentTarget.dataset.id;
    // 这里可以跳转到订单详情页面
    // wx.navigateTo({
    //   url: `/pages/order-detail/order-detail?id=${orderId}`
    // });
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
      
      if (userRole !== '1') {
        wx.showToast({
          title: '无权限访问订单页面',
          icon: 'none'
        });
        return;
      }
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
  },
  
  /**
   * 联系客服
   * 点击联系客服按钮时调用，弹出客服电话号码模态框
   */
  contactCaregiver: function(e) {
    const phoneNumber = e.currentTarget.dataset.phone;
    
    // 检查是否有电话号码
    if (!phoneNumber) {
      wx.showToast({
        title: '未获取到客服电话',
        icon: 'none'
      });
      return;
    }
    
    // 弹出模态框显示电话号码并提供拨打电话选项
    wx.showModal({
      title: '联系客服',
      content: `客服电话：${phoneNumber}`,
      showCancel: true,
      cancelText: '取消',
      confirmText: '拨打电话',
      success: (res) => {
        if (res.confirm) {
          // 用户确认拨打电话
          wx.makePhoneCall({
            phoneNumber: phoneNumber,
            success: () => {
              console.log('拨打电话成功');
            },
            fail: (error) => {
              console.error('拨打电话失败:', error);
              wx.showToast({
                title: '拨打电话失败',
                icon: 'none'
              });
            }
          });
        }
      },
      fail: (error) => {
        console.error('显示模态框失败:', error);
      }
    });
  }
});