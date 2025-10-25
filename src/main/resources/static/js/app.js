// 创建Vue实例
new Vue({
    el: '#app',
    data: {
        // 登录状态和管理员信息
        isLoggedIn: false,
        adminInfo: {},
        
        // 登录表单数据
        loginForm: {
            username: '',
            password: ''
        },
        loginRules: {
            username: [
                { required: true, message: '请输入用户名', trigger: 'blur' }
            ],
            password: [
                { required: true, message: '请输入密码', trigger: 'blur' }
            ]
        },
        
        // 激活的菜单
        activeMenu: '',
        
        // 订单相关数据
        orderList: [],
        orderTotal: 0,
        orderCurrentPage: 1,
        orderPageSize: 10,
        orderSearchForm: {
            orderNo: '',
            status: ''
        },
        
        // 用户相关数据
        userList: [],
        userTotal: 0,
        userCurrentPage: 1,
        userPageSize: 10,
        userSearchForm: {
            phone: '',
            name: ''
        },
        
        // 护工相关数据
        nurseList: [],
        nurseTotal: 0,
        nurseCurrentPage: 1,
        nursePageSize: 10,
        nurseSearchForm: {
            phone: '',
            name: ''
        },
        
        // 服务类型相关数据
        serviceTypeList: [],
        
        // 编辑护工对话框相关
        editNurseDialogVisible: false,
        editNurseForm: { id: '', serviceTypeId: '', status: '' },
        
        // 指派护工对话框
        assignNurseDialogVisible: false,
        assignNurseForm: {
            orderId: '',
            serviceTypeId: '',
            nurseId: '',
            startTime: '',
            endTime: ''
        },
        assignNurseRules: {
            serviceTypeId: [
                { required: true, message: '请选择服务类型', trigger: 'change' }
            ],
            nurseId: [
                { required: true, message: '请选择护工', trigger: 'change' }
            ]
        },
        freeNurseList: [],
        allNurseList: [],
        
        
        // 更新订单状态对话框（保留但不再使用）
        updateOrderStatusDialogVisible: false,
        updateOrderStatusForm: {
            orderId: '',
            currentStatus: '',
            newStatus: ''
        },
        updateOrderStatusRules: {
            newStatus: [
                { required: true, message: '请选择新状态', trigger: 'change' }
            ]
        },
        
        // 添加服务类型对话框
        addServiceTypeDialogVisible: false,
        addServiceTypeForm: {
            name: '',
            description: '',
            price: 0,
            duration: 0
        },
        addServiceTypeRules: {
            name: [
                { required: true, message: '请输入服务名称', trigger: 'blur' }
            ],
            price: [
                { required: true, message: '请输入服务价格', trigger: 'blur' },
                { type: 'number', min: 0, message: '服务价格必须大于等于0', trigger: 'blur' }
            ],
            duration: [
                { required: true, message: '请输入服务时长', trigger: 'blur' },
                { type: 'number', min: 1, message: '服务时长必须大于0', trigger: 'blur' }
            ]
        },
        
        // 编辑服务类型对话框
        editServiceTypeDialogVisible: false,
        editServiceTypeForm: {
            id: '',
            name: '',
            description: '',
            price: 0,
            duration: 0
        },
        editServiceTypeRules: {
            name: [
                { required: true, message: '请输入服务名称', trigger: 'blur' }
            ],
            price: [
                { required: true, message: '请输入服务价格', trigger: 'blur' },
                { type: 'number', min: 0, message: '服务价格必须大于等于0', trigger: 'blur' }
            ],
            duration: [
                { required: true, message: '请输入服务时长', trigger: 'blur' },
                { type: 'number', min: 1, message: '服务时长必须大于0', trigger: 'blur' }
            ]
        },
        
        // 表单标签宽度
        formLabelWidth: '120px',
        
        // 编辑订单对话框
        editOrderDialogVisible: false,
        editOrderForm: {
            id: '',
            serviceTypeId: '',
            startTime: '',
            endTime: '',
            serviceAddress: ''
        },
        // editOrderRules移到computed中，因为需要引用methods中的验证方法
    },
    
    // 计算属性
    computed: {
        editOrderRules() {
            return {
                serviceTypeId: [
                    { required: true, message: '请选择服务类型', trigger: 'change' }
                ],
                startTime: [
                    { required: true, message: '请选择服务开始时间', trigger: 'change' },
                    {
                        validator: this.validateStartTime,
                        trigger: ['change', 'blur']
                    }
                ],
                endTime: [
                    { required: true, message: '请选择服务结束时间', trigger: 'change' },
                    {
                        validator: this.validateEndTime,
                        trigger: ['change', 'blur']
                    }
                ],
                serviceAddress: [
                    { required: true, message: '请输入服务地址', trigger: 'blur' }
                ]
            };
        }
    },
    
    // 组件挂载后执行
    mounted() {
        // 从localStorage获取登录状态
        const token = localStorage.getItem('token');
        if (token) {
            this.isLoggedIn = true;
            // 获取管理员信息
            this.getAdminInfo();
            // 加载服务类型列表
            this.loadServiceTypes();
        }
    },
    
    methods: {
        // 验证开始时间
        validateStartTime(rule, value, callback) {
            console.log('验证开始时间:', value, '结束时间:', this.editOrderForm.endTime);
            if (value && this.editOrderForm.endTime) {
                // 确保日期格式正确解析
                const startTime = new Date(value);
                const endTime = new Date(this.editOrderForm.endTime);
                console.log('日期对象:', startTime, endTime, '比较结果:', startTime >= endTime);
                
                // 检查日期是否有效
                if (isNaN(startTime.getTime()) || isNaN(endTime.getTime())) {
                    console.error('无效的日期格式');
                    callback(); // 让其他规则处理格式问题
                } else if (startTime >= endTime) {
                    callback(new Error('开始时间不能大于等于结束时间'));
                } else {
                    callback();
                }
            } else {
                callback();
            }
        },
        
        // 验证结束时间
        validateEndTime(rule, value, callback) {
            console.log('验证结束时间:', value, '开始时间:', this.editOrderForm.startTime);
            if (value && this.editOrderForm.startTime) {
                // 确保日期格式正确解析
                const startTime = new Date(this.editOrderForm.startTime);
                const endTime = new Date(value);
                console.log('日期对象:', startTime, endTime, '比较结果:', startTime >= endTime);
                
                // 检查日期是否有效
                if (isNaN(startTime.getTime()) || isNaN(endTime.getTime())) {
                    console.error('无效的日期格式');
                    callback(); // 让其他规则处理格式问题
                } else if (startTime >= endTime) {
                    callback(new Error('开始时间不能大于等于结束时间'));
                } else {
                    callback();
                }
            } else {
                callback();
            }
        },
        
        // 管理员登录
        login() {
            this.$refs.loginForm.validate((valid) => {
                if (valid) {
                    // 构造查询参数
                    const params = new URLSearchParams();
                    params.append('username', this.loginForm.username);
                    params.append('password', this.loginForm.password);
                    
                    // 这里使用axios发送登录请求，以表单参数形式发送
                    axios.post('/homemaker/api/admin/login', params)
                        .then(response => {
                            if (response.data.code === 200) {
                                // 登录成功，保存token
                                localStorage.setItem('token', response.data.data.token);
                                this.isLoggedIn = true;
                                // 获取管理员信息
                                this.getAdminInfo();
                                // 加载服务类型列表
                                this.loadServiceTypes();
                                this.$message.success('登录成功');
                            } else {
                                this.$message.error(response.data.message);
                            }
                        })
                        .catch(error => {
                            console.error('登录失败:', error);
                            this.$message.error('登录失败，请稍后重试');
                        });
                }
            });
        },
        
        // 退出登录
        logout() {
            // 清除token和登录状态
            localStorage.removeItem('token');
            this.isLoggedIn = false;
            this.adminInfo = {};
            this.activeMenu = '';
            this.$message.success('退出登录成功');
        },
        
        // 获取管理员信息
        getAdminInfo() {
            const token = localStorage.getItem('token');
            axios.get('/homemaker/api/admin/info', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
            .then(response => {
                if (response.data.code === 200) {
                    this.adminInfo = response.data.data;
                }
            })
            .catch(error => {
                console.error('获取管理员信息失败:', error);
            });
        },
        
        // 根据服务类型ID获取服务类型名称
        getServiceTypeName(serviceTypeId) {
            if (!serviceTypeId) return '';
            const serviceType = this.serviceTypeList.find(item => item.id === serviceTypeId);
            return serviceType ? serviceType.name : `未知(${serviceTypeId})`;
        },
        
        // 格式化日期时间为yyyy-MM-dd hh:mm格式，并在年月日和时分之间添加换行
        formatDateTime(dateTime) {
            if (!dateTime) {
                return '';
            }
            // 截取到年月日时分，并在年月日和时分之间添加HTML换行标签
            const dateStr = dateTime.toString().substring(0, 16);
            // 在第10个字符位置（年月日之后）添加HTML换行标签
            return dateStr.substring(0, 10) + '<br/>' + dateStr.substring(11);
        },
        
        // 格式化服务时间
        formatServiceTime(startTime, endTime) {
            if (!startTime || !endTime) {
                return '';
            }
            // 格式化开始时间和结束时间为yyyy-MM-dd hh:mm格式
            const formattedStartTime = this.formatDateTime(startTime);
            const formattedEndTime = this.formatDateTime(endTime);
            // 在"至"两侧添加HTML换行标签
            return formattedStartTime + '<br/>至<br/>' + formattedEndTime;
        },
        
        // 菜单选择处理
        handleMenuSelect(key) {
            this.activeMenu = key;
            
            // 根据选择的菜单加载对应的数据
            if (key === '1-1') {
                // 先加载用户列表和护工列表，然后再加载订单列表
                // 这样在订单列表渲染时，userList和nurseList已有数据
                Promise.all([
                    this.loadUsersPromise(),
                    this.loadNursesPromise(),
                    Promise.resolve(this.loadServiceTypes())
                ]).then(() => {
                    this.loadOrders();
                });
            } else if (key === '2-1') {
                this.loadUsers();
            } else if (key === '3-1') {
                this.loadNurses();
            } else if (key === '4-1') {
                this.loadServiceTypes();
            }
        },
        
        // 加载订单列表
        loadOrders() {
            const token = localStorage.getItem('token');
            axios.get('/homemaker/api/order/list', {
                params: {
                    page: this.orderCurrentPage,
                    pageSize: this.orderPageSize,
                    orderNo: this.orderSearchForm.orderNo,
                    status: this.orderSearchForm.status,
                    username: this.orderSearchForm.username,
                    nurseName: this.orderSearchForm.nurseName,
                    serviceTypeId: this.orderSearchForm.serviceTypeId
                },
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
            .then(response => {
                if (response.data.code === 200) {
                    this.orderList = response.data.data.records;
                    this.orderTotal = response.data.data.total;
                } else {
                    this.$message.error(response.data.message);
                }
            })
            .catch(error => {
                console.error('加载订单列表失败:', error);
                this.$message.error('加载订单列表失败，请稍后重试');
            });
        },
        
        // 搜索订单
        searchOrders() {
            this.orderCurrentPage = 1;
            this.loadOrders();
        },
        
        // 订单分页大小改变
        handleOrderSizeChange(size) {
            this.orderPageSize = size;
            this.loadOrders();
        },
        
        // 订单当前页码改变
        handleOrderCurrentChange(current) {
            this.orderCurrentPage = current;
            this.loadOrders();
        },
        
        // 加载用户列表（调用后端接口）
        loadUsers() {
            this.loadUsersPromise();
        },
        
        // 加载用户列表的Promise版本，返回Promise以便在其他地方等待数据加载完成
        loadUsersPromise() {
            const token = localStorage.getItem('token');
            return axios.get('/homemaker/api/user/list', {
                params: {
                    pageNum: this.userCurrentPage,
                    pageSize: this.userPageSize,
                    phone: this.userSearchForm.phone,
                    name: this.userSearchForm.name
                },
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
            .then(response => {
                if (response.data.code === 200) {
                    this.userList = response.data.data.records;
                    this.userTotal = response.data.data.total;
                } else {
                    this.$message.error(response.data.message);
                    // 出现错误时使用模拟数据
                    this.userList = [
                        { id: 1, phone: '13800138001', name: '张三', address: '北京市海淀区中关村大街1号', createTime: '2025-01-01 10:00:00' },
                        { id: 2, phone: '13800138002', name: '李四', address: '北京市朝阳区建国路88号', createTime: '2025-01-02 11:00:00' },
                        { id: 3, phone: '13800138003', name: '王五', address: '上海市浦东新区张江高科技园区', createTime: '2025-01-03 12:00:00' },
                        { id: 4, phone: '13800138004', name: '赵六', address: '广州市天河区天河路385号', createTime: '2025-01-04 13:00:00' },
                        { id: 5, phone: '13800138005', name: '钱七', address: '深圳市南山区科技园南区', createTime: '2025-01-05 14:00:00' }
                    ];
                    this.userTotal = this.userList.length;
                }
                return response;
            })
            .catch(error => {
                console.error('加载用户列表失败:', error);
                this.$message.error('加载用户列表失败，请稍后重试');
                // 出现错误时使用模拟数据
                this.userList = [
                    { id: 1, phone: '13800138001', name: '张三', address: '北京市海淀区中关村大街1号', createTime: '2025-01-01 10:00:00' },
                    { id: 2, phone: '13800138002', name: '李四', address: '北京市朝阳区建国路88号', createTime: '2025-01-02 11:00:00' },
                    { id: 3, phone: '13800138003', name: '王五', address: '上海市浦东新区张江高科技园区', createTime: '2025-01-03 12:00:00' },
                    { id: 4, phone: '13800138004', name: '赵六', address: '广州市天河区天河路385号', createTime: '2025-01-04 13:00:00' },
                    { id: 5, phone: '13800138005', name: '钱七', address: '深圳市南山区科技园南区', createTime: '2025-01-05 14:00:00' }
                ];
                this.userTotal = this.userList.length;
                return Promise.reject(error);
            });
        },
        
        // 搜索用户
        searchUsers() {
            this.userCurrentPage = 1;
            this.loadUsers();
        },
        
        // 用户分页大小改变
        handleUserSizeChange(size) {
            this.userPageSize = size;
            this.loadUsers();
        },
        
        // 用户当前页码改变
        handleUserCurrentChange(current) {
            this.userCurrentPage = current;
            this.loadUsers();
        },
        
        // 加载护工列表（调用后端接口）
        loadNurses() {
            this.loadNursesPromise();
        },
        
        // 加载护工列表的Promise版本，返回Promise以便在其他地方等待数据加载完成
        loadNursesPromise() {
            const token = localStorage.getItem('token');
            return axios.get('/homemaker/api/nurse/list', {
                params: {
                    pageNum: this.nurseCurrentPage,
                    pageSize: this.nursePageSize,
                    phone: this.nurseSearchForm.phone,
                    name: this.nurseSearchForm.name
                },
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
            .then(response => {
                if (response.data.code === 200) {
                    this.nurseList = response.data.data.records;
                    this.nurseTotal = response.data.data.total;
                } else {
                    this.$message.error(response.data.message);
                    // 出现错误时使用模拟数据
                    this.nurseList = [
                        { id: 1, phone: '13900139001', name: '李护工', age: 45, serviceTypeId: 1, serviceTypeName: '居家护理', status: 1, rating: 4.8, serviceCount: 120, createTime: '2025-01-01 09:00:00' },
                        { id: 2, phone: '13900139002', name: '王护工', age: 38, serviceTypeId: 2, serviceTypeName: '康复训练', status: 2, rating: 4.6, serviceCount: 85, createTime: '2025-01-02 10:00:00' },
                        { id: 3, phone: '13900139003', name: '张护工', age: 52, serviceTypeId: 1, serviceTypeName: '居家护理', status: 1, rating: 4.9, serviceCount: 150, createTime: '2025-01-03 11:00:00' },
                        { id: 4, phone: '13900139004', name: '赵护工', age: 41, serviceTypeId: 3, serviceTypeName: '专业陪护', status: 3, rating: 4.7, serviceCount: 92, createTime: '2025-01-04 12:00:00' },
                        { id: 5, phone: '13900139005', name: '刘护工', age: 36, serviceTypeId: 2, serviceTypeName: '康复训练', status: 1, rating: 4.5, serviceCount: 68, createTime: '2025-01-05 13:00:00' }
                    ];
                    this.nurseTotal = this.nurseList.length;
                }
                return response;
            })
            .catch(error => {
                console.error('加载护工列表失败:', error);
                this.$message.error('加载护工列表失败，请稍后重试');
                // 出现错误时使用模拟数据
                this.nurseList = [
                    { id: 1, phone: '13900139001', name: '李护工', age: 45, serviceTypeId: 1, serviceTypeName: '居家护理', status: 1, rating: 4.8, serviceCount: 120, createTime: '2025-01-01 09:00:00' },
                    { id: 2, phone: '13900139002', name: '王护工', age: 38, serviceTypeId: 2, serviceTypeName: '康复训练', status: 2, rating: 4.6, serviceCount: 85, createTime: '2025-01-02 10:00:00' },
                    { id: 3, phone: '13900139003', name: '张护工', age: 52, serviceTypeId: 1, serviceTypeName: '居家护理', status: 1, rating: 4.9, serviceCount: 150, createTime: '2025-01-03 11:00:00' },
                    { id: 4, phone: '13900139004', name: '赵护工', age: 41, serviceTypeId: 3, serviceTypeName: '专业陪护', status: 3, rating: 4.7, serviceCount: 92, createTime: '2025-01-04 12:00:00' },
                    { id: 5, phone: '13900139005', name: '刘护工', age: 36, serviceTypeId: 2, serviceTypeName: '康复训练', status: 1, rating: 4.5, serviceCount: 68, createTime: '2025-01-05 13:00:00' }
                ];
                this.nurseTotal = this.nurseList.length;
                return Promise.reject(error);
            });
        },
        
        // 搜索护工
        searchNurses() {
            this.nurseCurrentPage = 1;
            this.loadNurses();
        },
        
        // 护工分页大小改变
        handleNurseSizeChange(size) {
            this.nursePageSize = size;
            this.loadNurses();
        },
        
        // 护工当前页码改变
        handleNurseCurrentChange(current) {
            this.nurseCurrentPage = current;
            this.loadNurses();
        },
        
        // 编辑护工信息
        editNurse(nurse) {
            // 初始化编辑表单
            this.editNurseForm = {
                id: nurse.id,
                serviceTypeId: nurse.serviceTypeId,
                status: nurse.status
            };
            // 显示编辑对话框
            this.editNurseDialogVisible = true;
        },
        
        // 提交编辑护工信息
        submitEditNurse() {
            const token = localStorage.getItem('token');
            const nurseData = {
                id: this.editNurseForm.id,
                serviceTypeId: this.editNurseForm.serviceTypeId,
                status: this.editNurseForm.status
            };
            
            axios.post('/homemaker/api/nurse/update', nurseData, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
            .then(response => {
                if (response.data.code === 200) {
                    this.$message.success('更新成功');
                    this.editNurseDialogVisible = false;
                    this.loadNurses(); // 重新加载护工列表
                } else {
                    this.$message.error(response.data.message);
                }
            })
            .catch(error => {
                console.error('更新护工信息失败:', error);
                this.$message.error('更新失败，请稍后重试');
            });
        },
        
        // 编辑订单
        editOrder(order) {
            this.editOrderForm = {
                id: order.id,
                orderNo: order.orderNo,
                serviceTypeId: order.serviceTypeId,
                startTime: order.startTime,
                endTime: order.endTime,
                serviceAddress: order.serviceAddress
            };
            this.editOrderDialogVisible = true;
        },
        

        
        // 确认编辑订单
        confirmEditOrder() {
            console.log('开始验证表单');
            this.$refs.editOrderForm.validate((valid) => {
                console.log('表单验证结果:', valid);
                if (valid) {
                    const token = localStorage.getItem('token');
                    axios.post('/homemaker/api/order/update', this.editOrderForm, {
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    })
                    .then(response => {
                        if (response.data.code === 200) {
                            this.editOrderDialogVisible = false;
                            this.loadOrders();
                            this.$message.success('编辑订单成功');
                        } else {
                            this.$message.error(response.data.message);
                        }
                    })
                    .catch(error => {
                        console.error('编辑订单失败:', error);
                        this.$message.error('编辑订单失败，请稍后重试');
                    });
                }
            });
        },
        
        // 删除订单
        deleteOrder(orderId) {
            this.$confirm('确定要删除该订单吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                const token = localStorage.getItem('token');
                axios.post(`/homemaker/api/order/delete?id=${orderId}`, {}, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                })
                .then(response => {
                    if (response.data.code === 200) {
                        this.loadOrders();
                        this.$message.success('删除订单成功');
                    } else {
                        this.$message.error(response.data.message);
                    }
                })
                .catch(error => {
                    console.error('删除订单失败:', error);
                    this.$message.error('删除订单失败，请稍后重试');
                });
            }).catch(() => {
                this.$message.info('已取消删除');
            });
        },
        
        // 取消订单
        cancelOrder(orderId) {
            this.$confirm('确定要取消该订单吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                const token = localStorage.getItem('token');
                axios.post('/homemaker/api/order/status', null, {
                    params: {
                        id: orderId,
                        status: 6 // 6表示已取消
                    },
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                })
                .then(response => {
                    if (response.data.code === 200) {
                        this.loadOrders();
                        this.$message.success('取消订单成功');
                    } else {
                        this.$message.error(response.data.message);
                    }
                })
                .catch(error => {
                    console.error('取消订单失败:', error);
                    this.$message.error('取消订单失败，请稍后重试');
                });
            }).catch(() => {
                this.$message.info('已取消操作');
            });
        },
        
        // 更新订单为已完成
        completeOrder(orderId) {
            this.$confirm('确定要将该订单标记为已完成吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'success'
            }).then(() => {
                const token = localStorage.getItem('token');
                axios.post('/homemaker/api/order/status', null, {
                    params: {
                        id: orderId,
                        status: 5 // 5表示已完成
                    },
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                })
                .then(response => {
                    if (response.data.code === 200) {
                        this.loadOrders();
                        this.$message.success('订单已更新为已完成');
                    } else {
                        this.$message.error(response.data.message);
                    }
                })
                .catch(error => {
                    console.error('更新订单状态失败:', error);
                    this.$message.error('更新订单状态失败，请稍后重试');
                });
            }).catch(() => {
                this.$message.info('已取消操作');
            });
        },
        
        // 评价订单
        evaluateOrder(orderId) {
            this.$message.info('评价功能暂未实现');
        },
        
        // 加载服务类型列表
        loadServiceTypes() {
            const token = localStorage.getItem('token');
            axios.get('/homemaker/api/service-type/list', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
            .then(response => {
                if (response.data.code === 200) {
                    // 清空现有列表并逐个添加新数据，确保响应式更新
                    this.serviceTypeList.splice(0, this.serviceTypeList.length);
                    if (response.data.data && response.data.data.length > 0) {
                        response.data.data.forEach(item => {
                            this.serviceTypeList.push(item);
                        });
                    }
                } else {
                    this.$message.error(response.data.message);
                }
            })
            .catch(error => {
                console.error('加载服务类型列表失败:', error);
                // 使用模拟数据，确保响应式更新
                this.serviceTypeList.splice(0, this.serviceTypeList.length);
                this.serviceTypeList.push(
                    { id: 1, name: '家政保洁', description: '提供家庭日常清洁服务', price: 80, duration: 90, createTime: '2025-01-01 08:00:00' },
                    { id: 2, name: '老人照料', description: '为老人提供日常生活照料服务', price: 120, duration: 120, createTime: '2025-01-01 08:00:00' },
                    { id: 3, name: '病人护理', description: '为病人提供专业护理服务', price: 150, duration: 120, createTime: '2025-01-01 08:00:00' }
                );
            });
        },
        
        // 根据用户ID获取用户名
        getUserName(userId) {
            if (!userId) return '未分配';
            const user = this.userList.find(item => item.id === userId);
            return user ? user.name : `未知(用户ID:${userId})`;
        },
        
        // 根据护工ID获取护工名
        getNurseName(nurseId) {
            if (!nurseId) return '未分配';
            const nurse = this.nurseList.find(item => item.id === nurseId);
            return nurse ? nurse.name : `未知(护工ID:${nurseId})`;
        },
        
        // 根据服务类型ID获取服务类型名称
        getServiceTypeName(serviceTypeId) {
            if (!serviceTypeId) return '未设置';
            const serviceType = this.serviceTypeList.find(item => item.id === serviceTypeId);
            return serviceType ? serviceType.name : `未知(服务类型ID:${serviceTypeId})`;
        },
        
        // 指派护工
        assignNurse(orderId) {
            // 获取订单信息，设置服务类型
            const order = this.orderList.find(item => item.id === orderId);
            if (order) {
                this.assignNurseForm = {
                    orderId: orderId,
                    serviceTypeId: order.serviceTypeId || '',
                    nurseId: '',
                    startTime: order.startTime || '',
                    endTime: order.endTime || ''
                };
                
                // 如果有服务类型，获取所有护工及其空闲状态
                if (order.serviceTypeId) {
                    this.getNursesWithAvailability();
                }
                
                this.assignNurseDialogVisible = true;
            }
        },
        
        // 获取护工列表及其空闲状态
        getNursesWithAvailability() {
            const token = localStorage.getItem('token');
            axios.get('/homemaker/api/nurse/free', {
                params: {
                    serviceTypeId: this.assignNurseForm.serviceTypeId,
                    startTime: this.assignNurseForm.startTime,
                    endTime: this.assignNurseForm.endTime
                },
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
            .then(response => {
                if (response.data.code === 200) {
                    // 后端返回所有服务类型匹配的护工，包含isFree属性
                    this.allNurseList = response.data.data;
                } else {
                    this.$message.error(response.data.message);
                    // 使用模拟数据
                    this.allNurseList = this.nurseList
                        .filter(item => item.serviceTypeId === this.assignNurseForm.serviceTypeId)
                        .map(item => ({
                            ...item,
                            isFree: item.status === 1 // 简单假设状态为1的护工是空闲的
                        }));
                }
            })
            .catch(error => {
                console.error('获取护工列表失败:', error);
                // 使用模拟数据
                this.allNurseList = this.nurseList
                    .filter(item => item.serviceTypeId === this.assignNurseForm.serviceTypeId)
                    .map(item => ({
                        ...item,
                        isFree: item.status === 1 // 简单假设状态为1的护工是空闲的
                    }));
            });
        },
        
        // 确认指派护工
        confirmAssignNurse() {
            this.$refs.assignNurseForm.validate((valid) => {
                if (valid) {
                    const token = localStorage.getItem('token');
                    // 修改为使用URL查询参数发送参数
                    axios.post('/homemaker/api/order/assign', null, {
                        params: {
                            orderId: this.assignNurseForm.orderId,
                            nurseId: this.assignNurseForm.nurseId
                        },
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    })
                    .then(response => {
                        if (response.data.code === 200) {
                            this.assignNurseDialogVisible = false;
                            this.loadOrders();
                            this.$message.success('指派护工成功');
                        } else {
                            this.$message.error(response.data.message);
                        }
                    })
                    .catch(error => {
                        console.error('指派护工失败:', error);
                        this.$message.error('指派护工失败，请稍后重试');
                    });
                }
            });
        },
        
        // 更新订单状态
        updateOrderStatus(orderId) {
            // 获取订单信息，设置当前状态
            const order = this.orderList.find(item => item.id === orderId);
            if (order) {
                let currentStatusText = '';
                switch(order.status) {
                    case 1: currentStatusText = '待派单'; break;
                    case 2: currentStatusText = '已派单'; break;
                    case 3: currentStatusText = '已接单'; break;
                    // 移除服务中状态
                    case 5: currentStatusText = '已完成'; break;
                    case 6: currentStatusText = '已取消'; break;
                    case 7: currentStatusText = '已拒绝'; break;
                    default: currentStatusText = '未知状态';
                }
                
                this.updateOrderStatusForm = {
                    orderId: orderId,
                    currentStatus: currentStatusText,
                    newStatus: ''
                };
                
                this.updateOrderStatusDialogVisible = true;
            }
        },
        
        // 确认更新订单状态
        confirmUpdateOrderStatus() {
            this.$refs.updateOrderStatusForm.validate((valid) => {
                if (valid) {
                    const token = localStorage.getItem('token');
                    // 修改为使用URL查询参数发送参数，并确保参数名与后端匹配
                    axios.post('/homemaker/api/order/status', null, {
                        params: {
                            id: this.updateOrderStatusForm.orderId,
                            status: this.updateOrderStatusForm.newStatus
                        },
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    })
                    .then(response => {
                        if (response.data.code === 200) {
                            this.updateOrderStatusDialogVisible = false;
                            this.loadOrders();
                            this.$message.success('订单状态更新成功');
                        } else {
                            this.$message.error(response.data.message);
                        }
                    })
                    .catch(error => {
                        console.error('更新订单状态失败:', error);
                        this.$message.error('更新订单状态失败，请稍后重试');
                    });
                }
            });
        },
        
        // 显示添加服务类型对话框
        showAddServiceTypeDialog() {
            this.addServiceTypeForm = {
                name: '',
                description: '',
                price: 0,
                duration: 0
            };
            this.addServiceTypeDialogVisible = true;
        },
        
        // 确认添加服务类型
        confirmAddServiceType() {
            this.$refs.addServiceTypeForm.validate((valid) => {
                if (valid) {
                    const token = localStorage.getItem('token');
                    axios.post('/homemaker/api/service-type/add', this.addServiceTypeForm, {
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    })
                    .then(response => {
                        if (response.data.code === 200) {
                            this.addServiceTypeDialogVisible = false;
                            this.loadServiceTypes();
                            this.$message.success('添加服务类型成功');
                        } else {
                            this.$message.error(response.data.message);
                        }
                    })
                    .catch(error => {
                        console.error('添加服务类型失败:', error);
                        this.$message.error('添加服务类型失败，请稍后重试');
                    });
                }
            });
        },
        
        // 显示编辑服务类型对话框
        showEditServiceTypeDialog(row) {
            this.editServiceTypeForm = {
                id: row.id,
                name: row.name,
                description: row.description,
                price: row.price,
                duration: row.duration
            };
            this.editServiceTypeDialogVisible = true;
        },
        
        // 确认编辑服务类型
        confirmEditServiceType() {
            this.$refs.editServiceTypeForm.validate((valid) => {
                if (valid) {
                    const token = localStorage.getItem('token');
                    axios.post('/homemaker/api/service-type/update', this.editServiceTypeForm, {
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    })
                    .then(response => {
                        if (response.data.code === 200) {
                            this.editServiceTypeDialogVisible = false;
                            this.loadServiceTypes();
                            this.$message.success('编辑服务类型成功');
                        } else {
                            this.$message.error(response.data.message);
                        }
                    })
                    .catch(error => {
                        console.error('编辑服务类型失败:', error);
                        this.$message.error('编辑服务类型失败，请稍后重试');
                    });
                }
            });
        },
        
        // 删除服务类型
        deleteServiceType(id) {
            this.$confirm('确定要删除该服务类型吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                const token = localStorage.getItem('token');
                axios.post(`/homemaker/api/service-type/delete?id=${id}`, {}, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                })
                .then(response => {
                    if (response.data.code === 200) {
                        this.loadServiceTypes();
                        this.$message.success('删除服务类型成功');
                    } else {
                        this.$message.error(response.data.message);
                    }
                })
                .catch(error => {
                    console.error('删除服务类型失败:', error);
                    this.$message.error('删除服务类型失败，请稍后重试');
                });
            }).catch(() => {
                this.$message.info('已取消删除');
            });
        }
    }
});