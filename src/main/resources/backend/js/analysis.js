new Vue({
    el: '#sales-analysis-app',
    data() {
        return {
            loading: false,
            todayStats: {
                totalOrders: 0,
                totalSales: '0.00',
                validOrders: 0,
                validSales: '0.00',
                orderCompletionRate: '0.0'
            },
            weeklyStats: {
                totalOrders: 0,
                totalSales: '0.00',
                validOrders: 0,
                validSales: '0.00',
                orderCompletionRate: '0.0'
            },
            monthlyStats: {
                totalOrders: 0,
                totalSales: '0.00',
                validOrders: 0,
                validSales: '0.00',
                orderCompletionRate: '0.0'
            },
            allStats: {
                totalOrders: 0,
                totalSales: '0.00',
                validOrders: 0,
                validSales: '0.00',
                orderCompletionRate: '0.0'
            },
            customStats: {
                totalOrders: 0,
                totalSales: '0.00',
                validOrders: 0,
                validSales: '0.00',
                orderCompletionRate: '0.0',
                startTime: null,
                endTime: null
            },
            queryForm: {
                dateRange: []
            },
            currentStatsType: 'all',
            pickerOptions: {
                shortcuts: [{
                    text: '最近一周',
                    onClick(picker) {
                        const end = new Date();
                        const start = new Date();
                        start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
                        picker.$emit('pick', [start, end]);
                    }
                }, {
                    text: '最近一个月',
                    onClick(picker) {
                        const end = new Date();
                        const start = new Date();
                        start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
                        picker.$emit('pick', [start, end]);
                    }
                }, {
                    text: '最近三个月',
                    onClick(picker) {
                        const end = new Date();
                        const start = new Date();
                        start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
                        picker.$emit('pick', [start, end]);
                    }
                }]
            },
            salesChart: null,
            completionChart: null
        };
    },
    mounted() {
        this.initData();
        this.initCharts();
        window.addEventListener('resize', this.handleResize);
    },
    beforeDestroy() {
        window.removeEventListener('resize', this.handleResize);
    },
    methods: {
        async initData() {
            this.loading = true;
            try {
                await Promise.all([
                    this.loadAllStats(),
                    this.loadTodayStats(),
                    this.loadWeeklyStats(),
                    this.loadMonthlyStats()
                ]);
                this.updateCharts();
            } catch (error) {
                console.error('初始化数据失败:', error);
                this.$message.error('加载数据失败，请重试');
            } finally {
                this.loading = false;
            }
        },

        async loadAllStats() {
            try {
                const res = await axios.get('/sales/all');
                console.log('全部统计数据响应:', res.data);
                if (res.data.code === 1) {
                    this.allStats = this.formatStatsData(res.data.data);
                    console.log('格式化后的全部统计数据:', this.allStats);
                } else {
                    console.error('获取全部统计数据失败:', res.data.msg);
                }
            } catch (error) {
                console.error('加载全部统计失败:', error);
            }
        },

        async loadTodayStats() {
            try {
                const res = await axios.get('/sales/today');
                console.log('今日统计数据响应:', res.data);
                if (res.data.code === 1) {
                    this.todayStats = this.formatStatsData(res.data.data);
                    console.log('格式化后的今日统计数据:', this.todayStats);
                } else {
                    console.error('获取今日统计数据失败:', res.data.msg);
                }
            } catch (error) {
                console.error('加载今日统计失败:', error);
            }
        },

        async loadWeeklyStats() {
            try {
                const res = await axios.get('/sales/weekly');
                console.log('本周统计数据响应:', res.data);
                if (res.data.code === 1) {
                    this.weeklyStats = this.formatStatsData(res.data.data);
                    console.log('格式化后的本周统计数据:', this.weeklyStats);
                } else {
                    console.error('获取本周统计数据失败:', res.data.msg);
                }
            } catch (error) {
                console.error('加载本周统计失败:', error);
            }
        },

        async loadMonthlyStats() {
            try {
                const res = await axios.get('/sales/monthly');
                console.log('本月统计数据响应:', res.data);
                if (res.data.code === 1) {
                    this.monthlyStats = this.formatStatsData(res.data.data);
                    console.log('格式化后的本月统计数据:', this.monthlyStats);
                } else {
                    console.error('获取本月统计数据失败:', res.data.msg);
                }
            } catch (error) {
                console.error('加载本月统计失败:', error);
            }
        },

        async queryCustomData() {
            if (!this.queryForm.dateRange || this.queryForm.dateRange.length !== 2) {
                this.$message.warning('请选择时间范围');
                return;
            }

            this.loading = true;
            try {
                const [startTime, endTime] = this.queryForm.dateRange;
                const res = await axios.get('/sales/summary', {
                    params: {
                        startTime: this.formatDateForApi(startTime),
                        endTime: this.formatDateForApi(endTime)
                    }
                });

                if (res.data.code === 1) {
                    this.customStats = this.formatStatsData(res.data.data);
                    this.customStats.startTime = this.formatDate(startTime);
                    this.customStats.endTime = this.formatDate(endTime);
                    this.currentStatsType = 'custom';
                } else {
                    this.$message.error(res.data.msg || '查询失败');
                }
            } catch (error) {
                console.error('查询失败:', error);
                this.$message.error('查询失败，请重试');
            } finally {
                this.loading = false;
            }
        },

        resetQuery() {
            this.queryForm.dateRange = [];
            this.customStats = {
                totalOrders: 0,
                totalSales: '0.00',
                validOrders: 0,
                validSales: '0.00',
                orderCompletionRate: '0.0',
                startTime: null,
                endTime: null
            };
            this.currentStatsType = 'all';
        },

        refreshData() {
            this.initData();
        },

        getCurrentStats() {
            switch (this.currentStatsType) {
                case 'custom':
                    return this.customStats;
                case 'all':
                default:
                    return this.allStats;
            }
        },

        getDetailStatsTitle() {
            switch (this.currentStatsType) {
                case 'custom':
                    if (this.customStats.startTime && this.customStats.endTime) {
                        return `自定义统计 (${this.customStats.startTime} - ${this.customStats.endTime})`;
                    }
                    return '自定义统计';
                case 'all':
                default:
                    return '全部订单统计';
            }
        },

        formatStatsData(data) {
            console.log('原始数据:', data);

            if (!data) {
                console.log('数据为空，返回默认值');
                return {
                    totalOrders: 0,
                    totalSales: '0.00',
                    validOrders: 0,
                    validSales: '0.00',
                    orderCompletionRate: '0.0'
                };
            }

            const formatted = {
                totalOrders: data.totalOrders || 0,
                totalSales: this.formatMoney(data.totalSales),
                validOrders: data.validOrders || 0,
                validSales: this.formatMoney(data.validSales),
                orderCompletionRate: this.formatPercent(data.orderCompletionRate)
            };

            console.log('格式化后的数据:', formatted);
            return formatted;
        },

        formatMoney(amount) {
            if (!amount) return '0.00';
            if (typeof amount === 'string') {
                return parseFloat(amount).toFixed(2);
            }
            return amount.toFixed(2);
        },

        formatPercent(percent) {
            if (!percent) return '0.0';
            if (typeof percent === 'string') {
                return parseFloat(percent).toFixed(1);
            }
            return percent.toFixed(1);
        },

        formatDate(date) {
            if (!date) return '';
            const d = new Date(date);
            return d.getFullYear() + '-' +
                String(d.getMonth() + 1).padStart(2, '0') + '-' +
                String(d.getDate()).padStart(2, '0');
        },

        formatDateForApi(date) {
            if (!date) return '';
            const d = new Date(date);
            return d.getFullYear() + '-' +
                String(d.getMonth() + 1).padStart(2, '0') + '-' +
                String(d.getDate()).padStart(2, '0') + ' 00:00:00';
        },

        initCharts() {
            this.$nextTick(() => {
                this.initSalesChart();
                this.initCompletionChart();
            });
        },

        initSalesChart() {
            const chartDom = document.getElementById('sales-chart');
            if (!chartDom) return;

            this.salesChart = echarts.init(chartDom);
            const option = {
                title: {
                    text: '销售额对比',
                    left: 'center',
                    textStyle: {
                        fontSize: 16,
                        fontWeight: 'bold'
                    }
                },
                tooltip: {
                    trigger: 'axis',
                    formatter: function(params) {
                        return params[0].name + '<br/>' +
                            params[0].seriesName + ': ¥' + params[0].value;
                    }
                },
                grid: {
                    left: '3%',
                    right: '4%',
                    bottom: '3%',
                    containLabel: true
                },
                xAxis: {
                    type: 'category',
                    data: ['今日', '本周', '本月'],
                    axisTick: {
                        alignWithLabel: true
                    }
                },
                yAxis: {
                    type: 'value',
                    axisLabel: {
                        formatter: '¥{value}'
                    }
                },
                series: [{
                    name: '销售额',
                    type: 'bar',
                    barWidth: '60%',
                    data: [0, 0, 0],
                    itemStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            {offset: 0, color: '#83bff6'},
                            {offset: 0.5, color: '#188df0'},
                            {offset: 1, color: '#188df0'}
                        ])
                    }
                }]
            };
            this.salesChart.setOption(option);
        },

        initCompletionChart() {
            const chartDom = document.getElementById('completion-chart');
            if (!chartDom) return;

            this.completionChart = echarts.init(chartDom);
            const option = {
                title: {
                    text: '今日订单完成率',
                    left: 'center',
                    textStyle: {
                        fontSize: 16,
                        fontWeight: 'bold'
                    }
                },
                tooltip: {
                    trigger: 'item',
                    formatter: function(params) {
                        return params.seriesName + '<br/>' +
                            params.name + ': ' + params.value + ' (' + params.percent + '%)';
                    }
                },
                legend: {
                    orient: 'vertical',
                    left: 'left',
                    data: ['已完成', '未完成']
                },
                series: [{
                    name: '订单状态',
                    type: 'pie',
                    radius: ['40%', '70%'],
                    center: ['50%', '60%'],
                    data: [
                        {value: 0, name: '已完成', itemStyle: {color: '#67C23A'}},
                        {value: 0, name: '未完成', itemStyle: {color: '#F56C6C'}}
                    ]
                }]
            };
            this.completionChart.setOption(option);
        },

        updateCharts() {
            if (this.salesChart) {
                this.salesChart.setOption({
                    series: [{
                        data: [
                            parseFloat(this.todayStats.validSales),
                            parseFloat(this.weeklyStats.validSales),
                            parseFloat(this.monthlyStats.validSales)
                        ]
                    }]
                });
            }

            if (this.completionChart) {
                const totalOrders = this.todayStats.totalOrders;
                const validOrders = this.todayStats.validOrders;
                const uncompletedOrders = totalOrders - validOrders;

                this.completionChart.setOption({
                    series: [{
                        data: [
                            {value: validOrders, name: '已完成', itemStyle: {color: '#67C23A'}},
                            {value: uncompletedOrders, name: '未完成', itemStyle: {color: '#F56C6C'}}
                        ]
                    }]
                });
            }
        },

        handleResize() {
            if (this.salesChart) {
                this.salesChart.resize();
            }
            if (this.completionChart) {
                this.completionChart.resize();
            }
        }
    }
});