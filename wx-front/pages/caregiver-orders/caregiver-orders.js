// caregiver-orders.js

/**
 * 护工订单页面
 * 展示护工相关的订单列表，包括全部、待接单、已接单、已完成等状态的订单
 */

// 引入API模块
import { request, API } from '../../utils/api';

Page({
  data: {
    // 订单列表数据
    orderList: [],
    
    // 当前页码，用于分页加载
    page: 1,
    
    // 每页显示条数
    pageSize: 10,
    
    // 是否还有更多数据
    hasMore: true,
    
    // 是否正在加载中
    loading: false,
    
    // 是否正在加载更多
    loadingMore: false,
    
    // 当前选中的tab索引
    currentTab: 0,
    
    // 护工订单tab配置
    tabs: [
      { id: '0', name: '全部' },
      { id: '2', name: '待接单' },
      { id: '3', name: '已接单' },
      { id: '5', name: '已完成' },
      { id: '7', name: '已拒绝' }
    ],

    userId: wx.getStorageSync('userId') || '',
    
    // 刷新状态文本
    refreshText: '刷新中...',
    
    // 当前激活的底部导航tab
    activeTab: 'orders'
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    console.log('护工订单页面加载成功');
    // 页面加载时检查登录状态
    this.checkLoginStatus();
    
    // 初始加载订单列表
    this.loadOrderList();
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function() {
    console.log('护工订单页面显示');
    // 每次页面显示时重新加载数据，确保数据最新
    this.setData({
      page: 1,
      orderList: [],
      hasMore: true
    });
    this.loadOrderList();
  },

  /**
   * 检查用户登录状态
   */
  checkLoginStatus: function() {
    const token = wx.getStorageSync('token');
    const userRole = wx.getStorageSync('userRole');
    
    console.log('检查登录状态 - token:', !!token, 'role:', userRole);
    
    // 验证登录和角色权限
    if (!token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      wx.redirectTo({
        url: '/pages/login/login'
      });
      return false;
    }
    
    if (userRole !== '2') {
      wx.showToast({
        title: '无权限访问护工订单页面',
        icon: 'none'
      });
      wx.redirectTo({
        url: '/pages/index/index'
      });
      return false;
    }
    
    return true;
  },

  /**
   * 加载订单列表数据
   * @param {Boolean} refresh 是否为下拉刷新
   */
  loadOrderList: function(refresh = false) {
    // 如果正在加载或没有更多数据，则不执行操作
    if (this.data.loading || (!this.data.hasMore && !refresh)) {
      return;
    }
    
    // 如果是刷新操作，重置页码和状态
    if (refresh) {
      this.setData({
        loading: true,
        page: 1,
        hasMore: true,
        orderList: []
      });
    } else if (!this.data.loadingMore) {
      this.setData({
        loading: true
      });
    }
    
    // 获取当前选中的tab状态
    const status = this.data.tabs[this.data.currentTab].id;
    
    console.log('加载护工订单列表 - 状态:', status, '页码:', this.data.page);
    
    // 调用API获取订单列表
    request(API.order.wxList, {
      method: 'GET',
      data: {
        nurseId: this.data.userId,
        page: this.data.page,
        pageSize: this.data.pageSize,
        status: status !== '0' ? status : ''
      }
    }).then(res => {
      console.log('获取护工订单列表成功:', res);
      
      // 处理API响应数据
      if (res.code === 200 && res.data) {
        // 处理订单数据，格式化日期等
        // 过滤掉已取消的订单
        const filteredRecords = res.data.records.filter(order => order.status !== 6);
        
        const formattedOrders = filteredRecords.map(order => {
          // 计算服务时长
          let serviceDuration = 0;
          if (order.startTime && order.endTime) {
            const start = new Date(order.startTime);
            const end = new Date(order.endTime);
            const durationMs = end - start;
            serviceDuration = (durationMs / (1000 * 60 * 60)).toFixed(1);
          }
          
          return {
            ...order,
            createTime: this.formatDateTime(order.createTime),
            serviceTime: this.formatDateTime(order.serviceTime),
            statusText: this.getStatusText(order.status),
            // 添加字段映射
            serviceDuration: serviceDuration,
            address: order.serviceAddress || '',
            totalPrice: order.totalAmount || 0
          };
        });
        
        // 根据是刷新还是加载更多，决定如何更新数据
        const newOrderList = refresh ? formattedOrders : [...this.data.orderList, ...formattedOrders];
        
        // 更新数据
        this.setData({
          orderList: newOrderList,
          hasMore: newOrderList.length < res.data.total,
          loading: false,
          loadingMore: false,
          refreshText: '刷新成功'
        });
        
        // 如果是下拉刷新，结束刷新动画
        if (refresh) {
          wx.stopPullDownRefresh();
          // 2秒后恢复刷新文本
          setTimeout(() => {
            this.setData({ refreshText: '刷新中...' });
          }, 2000);
        }
      } else {
        // API返回错误
        console.error('获取护工订单列表失败:', res.message || '网络异常');
        this.setData({
          loading: false,
          loadingMore: false
        });
        
        // 如果是下拉刷新，结束刷新动画
        if (refresh) {
          wx.stopPullDownRefresh();
        }
        
        // 显示错误提示
        wx.showToast({
          title: res.message || '获取订单失败',
          icon: 'none'
        });
      }
    }).catch(error => {
      // 请求异常
      console.error('获取护工订单列表异常:', error);
      this.setData({
        loading: false,
        loadingMore: false
      });
      
      // 如果是下拉刷新，结束刷新动画
      if (refresh) {
        wx.stopPullDownRefresh();
      }
      
      // 显示错误提示
      wx.showToast({
        title: '网络异常，请稍后重试',
        icon: 'none'
      });
    });
  },

  /**
   * 格式化日期时间
   * @param {String} dateTime 日期时间字符串
   * @returns {String} 格式化后的日期时间
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
   * 根据订单状态获取状态文本
   * @param {String} status 订单状态码
   * @returns {String} 状态文本
   */
  getStatusText: function(status) {
    // 将状态转换为字符串以确保类型匹配
    const statusStr = String(status);
    const statusMap = {
      '1': '待支付',
      '2': '待接单',
      '3': '已接单',
      '4': '进行中',
      '5': '已完成',
      '6': '已取消',
      '7': '已拒绝'
    };
    
    return statusMap[statusStr] || '未知状态';
  },

  /**
   * 处理接受订单
   * @param {Object} e 事件对象
   */
  handleAcceptOrder: function(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.showLoading({ title: '处理中...' });
    
    // 调用后台修改订单状态接口 - 将参数放在URL查询参数中
    request(`${API.order.updateStatus}?id=${orderId}&status=3`, {
      method: 'POST'
    }).then(res => {
      wx.hideLoading();
      if (res.code === 200) {
        wx.showToast({ title: '接受订单成功' });
        // 重新加载订单列表以更新状态
        this.loadOrderList(true);
      } else {
        wx.showToast({ 
          title: res.message || '接受订单失败',
          icon: 'none'
        });
      }
    }).catch(error => {
      wx.hideLoading();
      console.error('接受订单异常:', error);
      wx.showToast({ 
        title: '网络异常，请稍后重试',
        icon: 'none'
      });
    });
  },

  /**
   * 处理拒绝订单
   * @param {Object} e 事件对象
   */
  handleRejectOrder: function(e) {
    const orderId = e.currentTarget.dataset.id;
    
    // 二次确认
    wx.showModal({
      title: '确认拒绝',
      content: '确定要拒绝该订单吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '处理中...' });
          
          // 调用后台修改订单状态接口 - 将参数放在URL查询参数中
          request(`${API.order.updateStatus}?id=${orderId}&status=7`, {
            method: 'POST'
          }).then(res => {
            wx.hideLoading();
            if (res.code === 200) {
              wx.showToast({ title: '拒绝订单成功' });
              // 重新加载订单列表以更新状态
              this.loadOrderList(true);
            } else {
              wx.showToast({ 
                title: res.message || '拒绝订单失败',
                icon: 'none'
              });
            }
          }).catch(error => {
            wx.hideLoading();
            console.error('拒绝订单异常:', error);
            wx.showToast({ 
              title: '网络异常，请稍后重试',
              icon: 'none'
            });
          });
        }
      }
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
    
    // 切换tab后重新加载订单列表，传入true表示刷新操作以避免重复数据
    this.loadOrderList(true);
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
   * 联系用户
   * 点击联系用户按钮时调用，弹出用户电话号码模态框
   */
  contactUser: function(e) {
    const phoneNumber = e.currentTarget.dataset.phone;
    
    // 检查是否有电话号码
    if (!phoneNumber) {
      wx.showToast({
        title: '未获取到用户电话',
        icon: 'none'
      });
      return;
    }
    
    // 弹出模态框显示电话号码并提供拨打电话选项
    wx.showModal({
      title: '联系用户',
      content: `用户电话：${phoneNumber}`,
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