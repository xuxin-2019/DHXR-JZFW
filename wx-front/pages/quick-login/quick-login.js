// quick-login.js

/**
 * 一键登录页面
 * 实现微信手机号一键登录功能，支持角色选择和协议确认
 */

// 导入API路由管理
import { API } from '../../utils/api';

Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 用户角色（1:用户，2:护工）
    role: '',
    // 是否同意协议
    agreeTerms: false,
    // 是否正在登录
    loading: false,
    // 错误信息
    errorMsg: ''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('一键登录页面加载');
  },

  /**
   * 处理角色选择变化
   * @param {Object} e - 事件对象
   */
  onRoleChange(e) {
    this.setData({
      role: e.detail.value
    });
    // 清除错误信息
    this.setData({
      errorMsg: ''
    });
  },

  /**
   * 切换协议同意状态
   */
  toggleAgreement() {
    this.setData({
      agreeTerms: !this.data.agreeTerms
    });
    // 清除错误信息
    this.setData({
      errorMsg: ''
    });
  },

  /**
   * 查看服务协议
   */
  viewServiceAgreement() {
    wx.showModal({
      title: '服务协议与隐私条款',
      content: '这里是服务协议的详细内容...',
      showCancel: true,
      cancelText: '关闭',
      confirmText: '我已阅读'
    });
  },

  /**
   * 查看隐私政策
   */
  viewPrivacyPolicy() {
    wx.showModal({
      title: '个人信息保护指引',
      content: '这里是个人信息保护指引的详细内容...',
      showCancel: true,
      cancelText: '关闭',
      confirmText: '我已阅读'
    });
  },

  /**
   * 处理获取手机号事件
   * @param {Object} e - 手机号授权事件对象
   */
  onGetPhoneNumber(e) {
    console.log('手机号授权结果:', e);
    
    // 清除之前的错误信息
    this.setData({
      errorMsg: ''
    });
    
    // 检查是否已选择角色和同意条款
    if (!this.data.role) {
      this.setData({
        errorMsg: '请选择用户角色'
      });
      return;
    }
    
    if (!this.data.agreeTerms) {
      this.setData({
        errorMsg: '请阅读并同意服务协议和隐私条款'
      });
      return;
    }
    
    // 检查用户是否授权
    if (e.detail.errMsg === 'getPhoneNumber:ok') {
      // 保存手机号授权数据
      this.setData({
        encryptedData: e.detail.encryptedData,
        iv: e.detail.iv
      });
      
      // 开始登录流程
      this.startLoginFlow(e.detail.encryptedData, e.detail.iv);
    } else {
      // 用户拒绝授权
      this.setData({
        errorMsg: '请授权获取手机号以完成登录'
      });
    }
  },

  /**
   * 开始登录流程
   * @param {string} encryptedData - 加密的手机号数据
   * @param {string} iv - 加密算法的初始向量
   */
  startLoginFlow(encryptedData, iv) {
    // 显示加载状态
    this.setData({
      loading: true
    });
    
    // 首先调用wx.login获取登录code
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          console.log('获取登录code成功:', loginRes.code);
          
          // 使用wx.getUserInfo替代wx.getUserProfile
          // 注意：这是兼容处理，在实际生产环境中可能需要根据微信小程序版本调整
          try {
            // 先尝试直接获取用户信息（基础信息）
            wx.getUserInfo({
              withCredentials: true,
              success: (userRes) => {
                console.log('获取用户信息成功:', userRes.userInfo);
                // 调用后台登录接口
                this.submitLogin(loginRes.code, userRes.userInfo, encryptedData, iv);
              },
              fail: (userErr) => {
                console.error('获取用户信息失败:', userErr);
                
                // 如果获取失败，使用空的用户信息继续登录
                // 这样用户可以先完成登录，后续再补充用户信息
                const emptyUserInfo = {
                  nickName: '用户' + loginRes.code.substring(0, 6),
                  avatarUrl: '',
                  gender: 0
                };
                console.log('使用默认用户信息继续登录');
                this.submitLogin(loginRes.code, emptyUserInfo, encryptedData, iv);
              }
            });
          } catch (e) {
            console.error('用户信息获取异常:', e);
            // 发生异常时使用默认信息
            const emptyUserInfo = {
              nickName: '用户' + loginRes.code.substring(0, 6),
              avatarUrl: '',
              gender: 0
            };
            this.submitLogin(loginRes.code, emptyUserInfo, encryptedData, iv);
          }
        } else {
          console.error('获取登录code失败:', loginRes);
          this.handleLoginError('获取登录凭证失败，请重试');
        }
      },
      fail: (loginError) => {
        console.error('登录失败:', loginError);
        this.handleLoginError('网络异常，请检查网络连接');
      }
    });
  },

  /**
   * 提交登录信息到后台
   * @param {string} loginCode - 微信登录code
   * @param {Object} userInfo - 用户基本信息
   * @param {string} encryptedData - 加密的手机号数据
   * @param {string} iv - 加密算法的初始向量
   */
  submitLogin(loginCode, userInfo, encryptedData, iv) {
    // 构建登录参数
    const loginData = {
      code: loginCode,
      role: parseInt(this.data.role),
      userInfo: userInfo,
      encryptedData: encryptedData,
      iv: iv
    };
    
    console.log('提交登录数据:', loginData);
    
    // 调用后台登录接口
    wx.request({
      url: API.login,
      method: 'POST',
      data: loginData,
      success: (res) => {
        console.log('登录接口返回:', res.data);
        
        // 处理登录结果
        if (res.data.code === 200 && res.data.data) {
          // 登录成功，保存token和用户信息
          const token = res.data.data.token;
          const userInfo = res.data.data.userInfo || {};
          
          // 存储登录信息到本地
          wx.setStorageSync('token', token);
          wx.setStorageSync('userInfo', userInfo);
          wx.setStorageSync('userRole', this.data.role);
          wx.setStorageSync('userId', res.data.data.id);
          console.log('登录成功，准备跳转首页');
          
          // 显示登录成功提示
          wx.showToast({
            title: '登录成功',
            icon: 'success',
            duration: 1500
          });
          
          // 登录成功后返回首页 - 优化跳转逻辑
          // 使用较短的延迟确保用户能看到成功提示，但不会等待太久
          setTimeout(() => {
            console.log('执行页面跳转');
            try {
              // 先尝试使用switchTab（如果首页在tabBar配置中）
              wx.switchTab({
                url: '../index/index',
                fail: (switchTabError) => {
                  console.log('switchTab失败，尝试使用navigateTo:', switchTabError);
                  // 如果switchTab失败（可能首页不在tabBar中），使用navigateTo
                  wx.navigateTo({
                    url: '../index/index',
                    fail: (navigateToError) => {
                      console.error('navigateTo也失败:', navigateToError);
                      // 最后的尝试使用redirectTo
                      wx.redirectTo({
                        url: '../index/index'
                      });
                    }
                  });
                }
              });
            } catch (e) {
              console.error('页面跳转异常:', e);
            }
          }, 1000);
        } else {
          // 登录失败
          const errorMsg = res.data.message || '登录失败，请重试';
          this.handleLoginError(errorMsg);
        }
      },
      fail: (error) => {
        console.error('调用登录接口失败:', error);
        this.handleLoginError('服务器连接失败，请稍后重试');
      },
      complete: () => {
        // 无论成功失败，都关闭加载状态
        this.setData({
          loading: false
        });
      }
    });
  },

  /**
   * 处理登录错误
   * @param {string} errorMsg - 错误信息
   */
  handleLoginError(errorMsg) {
    this.setData({
      errorMsg: errorMsg,
      loading: false
    });
  }
});