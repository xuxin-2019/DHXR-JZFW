// util.js
// 通用工具函数

const formatTime = date => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  return `${[year, month, day].map(formatNumber).join('/')} ${[hour, minute, second].map(formatNumber).join(':')}`
}

const formatNumber = n => {
  n = n.toString()
  return n[1] ? n : `0${n}`
}

/**
 * 根据服务类型ID获取服务类型名称
 * @param {number|string} serviceTypeId - 服务类型ID
 * @returns {string} 服务类型名称
 */
export const getServiceTypeName = (serviceTypeId) => {
  if (!serviceTypeId) return '';
  
  try {
    const serviceTypes = wx.getStorageSync('serviceTypes') || [];
    const serviceType = serviceTypes.find(type => String(type.id) === String(serviceTypeId));
    return serviceType ? serviceType.name : '';
  } catch (error) {
    console.error('获取服务类型名称失败:', error);
    return '';
  }
};

/**
 * 确保服务类型列表已加载
 * 如果本地没有服务类型列表，则从服务器获取
 * @returns {Promise<Array>} 服务类型列表
 */
export const ensureServiceTypesLoaded = () => {
  return new Promise((resolve, reject) => {
    try {
      const existingTypes = wx.getStorageSync('serviceTypes');
      if (existingTypes && existingTypes.length > 0) {
        resolve(existingTypes);
        return;
      }
      
      // 从服务器获取服务类型列表
      const { API } = require('./api');
      wx.request({
        url: API.serviceType.list,
        method: 'GET',
        success: (res) => {
          if (res.data.code === 200 && res.data.data) {
            wx.setStorageSync('serviceTypes', res.data.data);
            resolve(res.data.data);
          } else {
            reject(new Error(res.data.message || '获取服务类型列表失败'));
          }
        },
        fail: (err) => {
          reject(err);
        }
      });
    } catch (error) {
      reject(error);
    }
  });
};

module.exports = {
  formatTime,
  getServiceTypeName,
  ensureServiceTypesLoaded
}
