// refund-apply.js

/**
 * 退款申请页面
 * 处理用户退款申请流程
 */

// 导入API路由管理
import { API, request } from '../../utils/api';
Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 订单ID
    orderId: '',
    // 订单信息
    orderInfo: null,
    // 退款金额
    refundAmount: '',
    // 退款原因类型
    refundReasonType: '',
    // 退款原因详情
    refundReasonDetail: '',
    // 上传凭证图片列表
    images: [],
    // 加载状态
    loading: false,
    // 提交中状态
    submitting: false,
    // 表单校验错误
    errors: {},
    // 退款原因选项
    reasonOptions: [
      { value: '1', label: '服务未按时上门' },
      { value: '2', label: '服务质量不满意' },
      { value: '3', label: '服务态度不好' },
      { value: '4', label: '服务内容与描述不符' },
      { value: '5', label: '其他原因' }
    ]
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    const orderId = options.orderId || '';
    if (!orderId) {
      wx.showToast({
        title: '订单信息错误',
        icon: 'none',
        complete: () => {
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        }
      });
      return;
    }
    
    this.setData({ orderId });
    this.getOrderDetail();
  },

  /**
   * 获取订单详情
   */
  getOrderDetail: function() {
    this.setData({ loading: true });
    
    // 由于接口可能未实现，这里模拟获取订单详情
    // 实际项目中应调用真实接口
    setTimeout(() => {
      // 模拟订单数据
      const mockOrderInfo = {
        orderId: this.data.orderId,
        orderNo: 'ORD20241212' + Math.random().toString(36).substr(2, 9).toUpperCase(),
        serviceTypeName: '家庭保洁',
        totalAmount: '100.00',
        serviceTime: '2024-12-25 14:00',
        serviceAddress: '北京市朝阳区某某小区1号楼101室',
        orderStatus: 2 // 已完成
      };
      
      this.setData({
        orderInfo: mockOrderInfo,
        refundAmount: mockOrderInfo.totalAmount,
        loading: false
      });
    }, 500);
  },

  /**
   * 选择退款原因类型
   */
  onReasonTypeChange: function(e) {
    const value = e.detail.value;
    this.setData({
      refundReasonType: value,
      errors: {}
    });
  },

  /**
   * 输入退款详情
   */
  onReasonDetailInput: function(e) {
    const value = e.detail.value;
    this.setData({
      refundReasonDetail: value
    });
  },

  /**
   * 上传凭证图片
   */
  uploadImage: function() {
    const { images } = this.data;
    
    if (images.length >= 5) {
      wx.showToast({
        title: '最多上传5张图片',
        icon: 'none'
      });
      return;
    }
    
    wx.chooseImage({
      count: 5 - images.length,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePaths = res.tempFilePaths;
        this.uploadFile(tempFilePaths);
      }
    });
  },

  /**
   * 上传文件到服务器
   */
  uploadFile: function(filePaths) {
    const { images } = this.data;
    let uploadedCount = 0;
    
    filePaths.forEach(path => {
      // 实际项目中应调用真实的上传接口
      // 这里模拟上传成功
      setTimeout(() => {
        const newImages = [...images, {
          url: path,
          id: Date.now().toString()
        }];
        
        this.setData({ images: newImages });
        uploadedCount++;
        
        if (uploadedCount === filePaths.length) {
          wx.showToast({
            title: '上传成功',
            icon: 'success'
          });
        }
      }, 300);
    });
  },

  /**
   * 删除图片
   */
  deleteImage: function(e) {
    const index = e.currentTarget.dataset.index;
    const { images } = this.data;
    
    wx.showModal({
      title: '提示',
      content: '确定要删除这张图片吗？',
      success: (res) => {
        if (res.confirm) {
          images.splice(index, 1);
          this.setData({ images });
        }
      }
    });
  },

  /**
   * 表单校验
   */
  validateForm: function() {
    const { refundReasonType, refundReasonDetail } = this.data;
    const errors = {};
    
    if (!refundReasonType) {
      errors.refundReasonType = '请选择退款原因';
    }
    
    if (!refundReasonDetail.trim()) {
      errors.refundReasonDetail = '请填写退款详情';
    } else if (refundReasonDetail.length > 200) {
      errors.refundReasonDetail = '退款详情不能超过200字';
    }
    
    this.setData({ errors });
    return Object.keys(errors).length === 0;
  },

  /**
   * 提交退款申请
   */
  submitRefund: function() {
    if (!this.validateForm()) {
      return;
    }
    
    const { orderId, refundAmount, refundReasonType, refundReasonDetail, images } = this.data;
    
    wx.showModal({
      title: '确认退款',
      content: `确定要申请退款${refundAmount}元吗？`,
      success: (res) => {
        if (res.confirm) {
          this.setData({ submitting: true });
          
          // 构建提交数据
          const submitData = {
            orderId,
            refundAmount: parseFloat(refundAmount),
            refundReasonType,
            refundReasonDetail,
            refundPics: images.map(img => img.url || '')
          };
          
          // 调用退款申请接口
          request(API.refund.apply, {
            method: 'POST',
            data: submitData
          }).then(res => {
            this.setData({ submitting: false });
            
            if (res.code === 200) {
              wx.showToast({
                title: '退款申请提交成功',
                icon: 'success',
                complete: () => {
                  setTimeout(() => {
                    wx.redirectTo({
                      url: `/pages/order-detail/order-detail?orderId=${orderId}`
                    });
                  }, 1500);
                }
              });
            } else {
              wx.showToast({
                title: res.message || '提交失败，请稍后重试',
                icon: 'none'
              });
            }
          }).catch(err => {
            console.error('提交退款申请失败:', err);
            this.setData({ submitting: false });
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
   * 预览图片
   */
  previewImage: function(e) {
    const index = e.currentTarget.dataset.index;
    const urls = this.data.images.map(img => img.url);
    
    wx.previewImage({
      current: urls[index],
      urls: urls
    });
  }
});