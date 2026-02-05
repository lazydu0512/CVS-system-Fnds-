<template>
  <div class="dashboard">
    <el-card class="overview-card">
      <template #header>
        <div class="card-header">
          <h3>数据总览</h3>
          <el-button @click="refreshData" :loading="loading" size="small">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <!-- 数据卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6" v-for="(stat, index) in overviewStats" :key="index">
          <div class="stat-card">
            <div class="stat-icon" :style="{ backgroundColor: stat.color }">
              <component :is="stat.icon" />
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 城市视频分布地图 - 全宽显示 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header><h4>城市视频分布（国内）</h4></template>
          <div class="map-container-wrapper">
            <v-chart v-if="isMapReady" class="chart-large" :option="cityDistributionOption" autoresize />
            <div v-else class="loading-placeholder-large">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span style="margin-left: 10px">地图数据加载中...</span>
            </div>
            <!-- 粤港澳大湾区局部放大图 -->
            <div v-if="isMapReady" class="bay-area-inset">
              <v-chart class="bay-area-chart" :option="bayAreaOption" autoresize />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 海外城市统计 -->
    <el-row v-if="overseasData.length > 0" :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <h4>海外城市热力榜</h4>
              <el-tag type="info" size="small">共 {{ overseasData.length }} 个海外城市</el-tag>
            </div>
          </template>
          <v-chart class="chart" :option="overseasCitiesOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表 - 垂直排列 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header><h4>视频浏览量趋势</h4></template>
          <v-chart class="chart" :option="viewsTrendOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header><h4>{{ isSystemAdmin ? '用户注册量趋势' : '视频上传量趋势' }}</h4></template>
          <v-chart class="chart" :option="isSystemAdmin ? registerTrendOption : uploadTrendOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header><h4>互动数据统计</h4></template>
          <v-chart class="chart" :option="interactionOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门视频 -->
    <el-card style="margin-top: 20px">
      <template #header><h4>热门视频排行 Top 10</h4></template>
      <div class="top-videos-grid">
        <div v-for="(video, index) in topVideos" :key="video.id" class="video-item">
          <div class="rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
          <img :src="getMediaUrl(video.thumbnailUrl)" :alt="video.title" class="video-thumbnail" />
          <div class="video-info">
            <div class="video-title">{{ video.title }}</div>
            <div class="video-meta">
              <span><el-icon><VideoPlay /></el-icon> {{ video.viewCount }}</span>
              <span><el-icon><Star /></el-icon> {{ video.likeCount }}</span>
              <span>{{ video.city }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import * as echarts from 'echarts'
import VChart from 'vue-echarts'
import { adminAPI } from '../api/video'
import { ElMessage } from 'element-plus'
import { Refresh, VideoPlay, Star, User, TrendCharts, View, DataLine, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getMediaUrl } from '../utils/mediaUrl'

// 动态导入中国地图数据
let chinaMapData = null

const userStore = useUserStore()
const loading = ref(false)
const isMapReady = ref(false) // 地图是否准备就绪
const isSystemAdmin = computed(() => userStore.user?.role === 1)

// 总览数据
const overview = reactive({
  totalVideos: 0,
  totalUsers: 0,
  totalViews: 0,
  totalLikes: 0,
  totalCollects: 0
})

const overviewStats = computed(() => {
  const stats = [
    { label: '总视频数', value: overview.totalVideos, icon: VideoPlay, color: '#409EFF' },
    { label: '总浏览量', value: overview.totalViews, icon: View, color: '#67C23A' }
  ]
  
  if (isSystemAdmin.value) {
    stats.push({ label: '总用户数', value: overview.totalUsers, icon: User, color: '#E6A23C' })
  }
  
  stats.push(
    { label: '总点赞数', value: overview.totalLikes, icon: Star, color: '#F56C6C' },
    { label: '总收藏数', value: overview.totalCollects, icon: DataLine, color: '#909399' }
  )
  
  return stats.slice(0, 4)
})

// 趋势数据
const viewsTrendData = ref([])
const registerTrendData = ref([])
const uploadTrendData = ref([])
const interactionData = ref({ likes: 0, collects: 0, comments: 0 })
const cityData = ref([])
const cityDetails = ref({}) // 省份 -> 城市明细映射
const overseasData = ref([]) // 海外城市数据
const topVideos = ref([])

// 城市名称映射：数据库名称 -> 地图标准名称
const cityNameMap = {
  // 直辖市
  '北京': '北京市',
  '上海': '上海市',
  '天津': '天津市',
  '重庆': '重庆市',
  
  // 省份（如果数据库里没有"省"字）
  '广东': '广东省',
  '浙江': '浙江省',
  '江苏': '江苏省',
  '山东': '山东省',
  '河南': '河南省',
  '四川': '四川省',
  '湖北': '湖北省',
  '湖南': '湖南省',
  '河北': '河北省',
  '福建': '福建省',
  '安徽': '安徽省',
  '江西': '江西省',
  '云南': '云南省',
  '贵州': '贵州省',
  '山西': '山西省',
  '陕西': '陕西省',
  '甘肃': '甘肃省',
  '青海': '青海省',
  '海南': '海南省',
  '辽宁': '辽宁省',
  '吉林': '吉林省',
  '黑龙江': '黑龙江省',
  '台湾': '台湾省',
  
  // 自治区（如果数据库里没有"自治区"）
  '内蒙古': '内蒙古自治区',
  '广西': '广西壮族自治区',
  '西藏': '西藏自治区',
  '宁夏': '宁夏回族自治区',
  '新疆': '新疆维吾尔自治区',
  
  // 特别行政区
  '香港': '香港特别行政区',
  '澳门': '澳门特别行政区',
  
  // === 市级城市到省份的映射 ===
  
  // 广东省主要城市
  '广州': '广东省', '深圳': '广东省', '珠海': '广东省', '汕头': '广东省',
  '佛山': '广东省', '韶关': '广东省', '湛江': '广东省', '肇庆': '广东省',
  '江门': '广东省', '茂名': '广东省', '惠州': '广东省', '梅州': '广东省',
  '汕尾': '广东省', '河源': '广东省', '阳江': '广东省', '清远': '广东省',
  '东莞': '广东省', '中山': '广东省', '潮州': '广东省', '揭阳': '广东省',
  '云浮': '广东省',
  
  // 浙江省主要城市
  '杭州': '浙江省', '宁波': '浙江省', '温州': '浙江省', '嘉兴': '浙江省',
  '湖州': '浙江省', '绍兴': '浙江省', '金华': '浙江省', '衢州': '浙江省',
  '舟山': '浙江省', '台州': '浙江省', '丽水': '浙江省',
  
  // 江苏省主要城市
  '南京': '江苏省', '无锡': '江苏省', '徐州': '江苏省', '常州': '江苏省',
  '苏州': '江苏省', '南通': '江苏省', '连云港': '江苏省', '淮安': '江苏省',
  '盐城': '江苏省', '扬州': '江苏省', '镇江': '江苏省', '泰州': '江苏省',
  '宿迁': '江苏省',
  
  // 山东省主要城市
  '济南': '山东省', '青岛': '山东省', '淄博': '山东省', '枣庄': '山东省',
  '东营': '山东省', '烟台': '山东省', '潍坊': '山东省', '济宁': '山东省',
  '泰安': '山东省', '威海': '山东省', '日照': '山东省', '临沂': '山东省',
  '德州': '山东省', '聊城': '山东省', '滨州': '山东省', '菏泽': '山东省',
  
  // 河南省主要城市
  '郑州': '河南省', '开封': '河南省', '洛阳': '河南省', '平顶山': '河南省',
  '安阳': '河南省', '鹤壁': '河南省', '新乡': '河南省', '焦作': '河南省',
  '濮阳': '河南省', '许昌': '河南省', '漯河': '河南省', '三门峡': '河南省',
  '南阳': '河南省', '商丘': '河南省', '信阳': '河南省', '周口': '河南省',
  '驻马店': '河南省',
  
  // 四川省主要城市
  '成都': '四川省', '自贡': '四川省', '攀枝花': '四川省', '泸州': '四川省',
  '德阳': '四川省', '绵阳': '四川省', '广元': '四川省', '遂宁': '四川省',
  '内江': '四川省', '乐山': '四川省', '南充': '四川省', '眉山': '四川省',
  '宜宾': '四川省', '广安': '四川省', '达州': '四川省', '雅安': '四川省',
  '巴中': '四川省', '资阳': '四川省',
  
  // 湖北省主要城市
  '武汉': '湖北省', '黄石': '湖北省', '十堰': '湖北省', '宜昌': '湖北省',
  '襄阳': '湖北省', '鄂州': '湖北省', '荆门': '湖北省', '孝感': '湖北省',
  '荆州': '湖北省', '黄冈': '湖北省', '咸宁': '湖北省', '随州': '湖北省',
  
  // 湖南省主要城市
  '长沙': '湖南省', '株洲': '湖南省', '湘潭': '湖南省', '衡阳': '湖南省',
  '邵阳': '湖南省', '岳阳': '湖南省', '常德': '湖南省', '张家界': '湖南省',
  '益阳': '湖南省', '郴州': '湖南省', '永州': '湖南省', '怀化': '湖南省',
  '娄底': '湖南省',
  
  // 河北省主要城市
  '石家庄': '河北省', '唐山': '河北省', '秦皇岛': '河北省', '邯郸': '河北省',
  '邢台': '河北省', '保定': '河北省', '张家口': '河北省', '承德': '河北省',
  '沧州': '河北省', '廊坊': '河北省', '衡水': '河北省',
  
  // 福建省主要城市
  '福州': '福建省', '厦门': '福建省', '莆田': '福建省', '三明': '福建省',
  '泉州': '福建省', '漳州': '福建省', '南平': '福建省', '龙岩': '福建省',
  '宁德': '福建省',
  
  // 安徽省主要城市
  '合肥': '安徽省', '芜湖': '安徽省', '蚌埠': '安徽省', '淮南': '安徽省',
  '马鞍山': '安徽省', '淮北': '安徽省', '铜陵': '安徽省', '安庆': '安徽省',
  '黄山': '安徽省', '滁州': '安徽省', '阜阳': '安徽省', '宿州': '安徽省',
  '六安': '安徽省', '亳州': '安徽省', '池州': '安徽省', '宣城': '安徽省',
  
  // 江西省主要城市
  '南昌': '江西省', '景德镇': '江西省', '萍乡': '江西省', '九江': '江西省',
  '新余': '江西省', '鹰潭': '江西省', '赣州': '江西省', '吉安': '江西省',
  '宜春': '江西省', '抚州': '江西省', '上饶': '江西省',
  
  // 云南省主要城市
  '昆明': '云南省', '曲靖': '云南省', '玉溪': '云南省', '保山': '云南省',
  '昭通': '云南省', '丽江': '云南省', '普洱': '云南省', '临沧': '云南省',
  
  // 贵州省主要城市
  '贵阳': '贵州省', '六盘水': '贵州省', '遵义': '贵州省', '安顺': '贵州省',
  
  // 其他省份主要城市
  '太原': '山西省', '大同': '山西省', '阳泉': '山西省', '长治': '山西省',
  '西安': '陕西省', '咸阳': '陕西省', '宝鸡': '陕西省', '渭南': '陕西省',
  '兰州': '甘肃省', '嘉峪关': '甘肃省', '金昌': '甘肃省', '白银': '甘肃省',
  '西宁': '青海省',
  '海口': '海南省', '三亚': '海南省',
  '沈阳': '辽宁省', '大连': '辽宁省', '鞍山': '辽宁省', '抚顺': '辽宁省',
  '长春': '吉林省', '吉林': '吉林省',
  '哈尔滨': '黑龙江省', '齐齐哈尔': '黑龙江省', '鸡西': '黑龙江省', '大庆': '黑龙江省',
  '台北': '台湾省', '高雄': '台湾省',
  '呼和浩特': '内蒙古自治区', '包头': '内蒙古自治区',
  '南宁': '广西壮族自治区', '柳州': '广西壮族自治区', '桂林': '广西壮族自治区',
  '拉萨': '西藏自治区',
  '银川': '宁夏回族自治区',
  '乌鲁木齐': '新疆维吾尔自治区'
}

// 标准化城市名称以匹配地图
const normalizeCityName = (cityName) => {
  if (!cityName) return { name: null, isOverseas: false }
  
  // 如果已经有后缀，直接返回
  if (cityName.endsWith('省') || 
      cityName.endsWith('市') || 
      cityName.endsWith('自治区') || 
      cityName.endsWith('特别行政区')) {
    return { name: cityName, isOverseas: false }
  }
  
  // 使用映射表
  const mapped = cityNameMap[cityName]
  if (mapped) {
    return { name: mapped, isOverseas: false }
  }
  
  // 如果没有映射，标记为海外城市
  console.log(`识别为海外城市: ${cityName}`)
  return { name: cityName, isOverseas: true }
}

// 视频浏览量趋势图配置
const viewsTrendOption = computed(() => ({
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: viewsTrendData.value.map(item => item.date.slice(5)),
    boundaryGap: false
  },
  yAxis: {
    type: 'value'
  },
  series: [{
    data: viewsTrendData.value.map(item => item.views),
    type: 'line',
    smooth: true,
    areaStyle: {
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [{
          offset: 0, color: 'rgba(64, 158, 255, 0.5)'
        }, {
          offset: 1, color: 'rgba(64, 158, 255, 0.1)'
        }]
      }
    },
    lineStyle: {
      color: '#409EFF',
      width: 3
    },
    itemStyle: {
      color: '#409EFF'
    }
  }]
}))

// 用户注册量趋势图配置
const registerTrendOption = computed(() => ({
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: registerTrendData.value.map(item => item.date.slice(5))
  },
  yAxis: {
    type: 'value'
  },
  series: [{
    data: registerTrendData.value.map(item => item.count),
    type: 'bar',
    itemStyle: {
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [{
          offset: 0, color: '#67C23A'
        }, {
          offset: 1, color: '#85CE61'
        }]
      }
    }
  }]
}))

// 视频上传量趋势图配置
const uploadTrendOption = computed(() => ({
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: uploadTrendData.value.map(item => item.date.slice(5)),
    boundaryGap: false
  },
  yAxis: {
    type: 'value'
  },
  series: [{
    data: uploadTrendData.value.map(item => item.count),
    type: 'line',
    smooth: true,
    areaStyle: {
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [{
          offset: 0, color: 'rgba(230, 162, 60, 0.5)'
        }, {
          offset: 1, color: 'rgba(230, 162, 60, 0.1)'
        }]
      }
    },
    lineStyle: {
      color: '#E6A23C',
      width: 3
    },
    itemStyle: {
      color: '#E6A23C'
    }
  }]
}))

// 互动数据饼图配置
const interactionOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    bottom: '5%',
    left: 'center'
  },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    avoidLabelOverlap: false,
    itemStyle: {
      borderRadius: 10,
      borderColor: '#fff',
      borderWidth: 2
    },
    label: {
      show: false
    },
    emphasis: {
      label: {
        show: true,
        fontSize: 20,
        fontWeight: 'bold'
      }
    },
    data: [
      { value: interactionData.value.likes, name: '点赞', itemStyle: { color: '#F56C6C' } },
      { value: interactionData.value.collects, name: '收藏', itemStyle: { color: '#E6A23C' } },
      { value: interactionData.value.comments, name: '评论', itemStyle: { color: '#409EFF' } }
    ]
  }]
}))

// 城市分布图配置（中国地图）
const cityDistributionOption = computed(() => {
  return {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        if (!params.data) return params.name
        
        const provinceName = params.name
        const totalCount = params.value
        const cities = cityDetails.value[provinceName] || []
        
        // 构建tooltip内容
        let tooltipHtml = `<div style="padding: 5px;">
          <strong style="font-size: 14px;">${provinceName}</strong><br/>
          <span style="color: #666;">总视频数: <strong>${totalCount}</strong></span><br/>`
        
        if (cities.length > 0) {
          tooltipHtml += `<div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid #eee;">
            <span style="color: #999; font-size: 12px;">城市明细：</span><br/>`
          
          cities.forEach(city => {
            tooltipHtml += `<span style="font-size: 12px; color: #666;">• ${city.name}: ${city.count}</span><br/>`
          })
          
          tooltipHtml += `</div>`
        }
        
        tooltipHtml += `</div>`
        return tooltipHtml
      }
    },
    visualMap: {
      min: 0,
      max: Math.max(...cityData.value.map(item => item.value), 10),
      text: ['高', '低'],
      realtime: false,
      calculable: true,
      textStyle: {
        fontSize: 14
      },
      inRange: {
        color: ['#e0f3f8', '#abd9e9', '#74add1', '#4575b4', '#313695']
      }
    },
    geo: {
      map: 'china',
      roam: true,
      zoom: 1.2,
      layoutCenter: ['50%', '60%'],
      layoutSize: '100%',
      label: {
        show: true,
        fontSize: 12,
        color: '#333'
      },
      itemStyle: {
        areaColor: '#f3f3f3',
        borderColor: '#999',
        borderWidth: 1.5
      },
      emphasis: {
        itemStyle: {
          areaColor: '#ffeb3b',
          borderWidth: 2
        },
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      // 隐藏香港澳门的标签
      regions: [
        {
          name: '香港特别行政区',
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: false
            }
          }
        },
        {
          name: '澳门特别行政区',
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: false
            }
          }
        }
      ]
    },
    series: [
      {
        name: '视频数量',
        type: 'map',
        map: 'china',
        geoIndex: 0,
        data: cityData.value
      }
    ]
  }
})

// 粤港澳大湾区局部放大图配置
const bayAreaOption = computed(() => {
  // 筛选出广东省、香港、澳门的数据
  const bayAreaData = cityData.value.filter(item => 
    item.name === '广东省' || 
    item.name === '香港特别行政区' || 
    item.name === '澳门特别行政区'
  )
  
  return {
    title: {
      text: '广东省',
      left: 'center',
      top: 5,
      textStyle: {
        fontSize: 10,
        fontWeight: 'bold',
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        if (!params.data) return params.name
        return `<div style="padding: 5px;">
          <strong>${params.name}</strong><br/>
          <span style="color: #666;">视频数: <strong>${params.value}</strong></span>
        </div>`
      }
    },
    geo: {
      map: 'china',
      roam: false,
      // 聚焦到香港澳门区域
      center: [113.9, 22.25],
      zoom: 60,
      label: {
        show: true,
        fontSize: 8,
        color: '#333'
      },
      itemStyle: {
        areaColor: '#f3f3f3',
        borderColor: '#999',
        borderWidth: 0.5
      },
      emphasis: {
        itemStyle: {
          areaColor: '#ffeb3b'
        },
        label: {
          show: true,
          fontSize: 9,
          fontWeight: 'bold'
        }
      },
      // 只显示相关区域
      regions: [
        {
          name: '广东省',
          itemStyle: {
            areaColor: '#e3f2fd'
          }
        },
        {
          name: '香港特别行政区',
          itemStyle: {
            areaColor: '#ffebee'
          }
        },
        {
          name: '澳门特别行政区',
          itemStyle: {
            areaColor: '#fff3e0'
          }
        }
      ]
    },
    series: [
      {
        name: '视频数量',
        type: 'map',
        map: 'china',
        geoIndex: 0,
        data: bayAreaData
      }
    ]
  }
})

// 海外城市柱状图配置
const overseasCitiesOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: '10%',
    right: '10%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'value'
  },
  yAxis: {
    type: 'category',
    data: overseasData.value.map(item => item.name),
    axisLabel: {
      interval: 0
    }
  },
  series: [{
    name: '视频数量',
    type: 'bar',
    data: overseasData.value.map(item => item.value),
    itemStyle: {
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 1,
        y2: 0,
        colorStops: [{
          offset: 0, color: '#667eea'
        }, {
          offset: 1, color: '#764ba2'
        }]
      }
    },
    label: {
      show: true,
      position: 'right',
      formatter: '{c}'
    }
  }]
}))

// 加载数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    // 加载总览数据
    const overviewRes = await adminAPI.getDashboardOverview()
    if (overviewRes.data.success) {
      Object.assign(overview, overviewRes.data.data)
    }

    // 加载视频浏览量趋势
    const viewsRes = await adminAPI.getVideoViewsTrend(30)
    if (viewsRes.data.success) {
      viewsTrendData.value = viewsRes.data.data
    }

    // 加载用户注册趋势（仅系统管理员）
    if (isSystemAdmin.value) {
      const registerRes = await adminAPI.getUserRegisterTrend(30)
      if (registerRes.data.success) {
        registerTrendData.value = registerRes.data.data
      }
    } else {
      // 城市管理员加载上传量趋势
      const uploadRes = await adminAPI.getVideoUploadTrend(30)
      if (uploadRes.data.success) {
        uploadTrendData.value = uploadRes.data.data
      }
    }

    // 加载互动数据
    const interactionRes = await adminAPI.getInteractionStats()
    if (interactionRes.data.success) {
      interactionData.value = interactionRes.data.data
    }

    // 加载城市分布
    const cityRes = await adminAPI.getCityDistribution()
    if (cityRes.data.success) {
      // 分离国内和海外城市数据，并记录城市明细
      const domesticMap = new Map()
      const overseasMap = new Map()
      const provinceDetails = {} // 存储每个省份的城市明细
      
      cityRes.data.data.forEach(item => {
        const result = normalizeCityName(item.name)
        if (result.name) {
          if (result.isOverseas) {
            // 海外城市
            const currentValue = overseasMap.get(result.name) || 0
            overseasMap.set(result.name, currentValue + Number(item.value))
          } else {
            // 国内省份
            const provinceName = result.name
            const currentValue = domesticMap.get(provinceName) || 0
            domesticMap.set(provinceName, currentValue + Number(item.value))
            
            // 记录城市明细
            if (!provinceDetails[provinceName]) {
              provinceDetails[provinceName] = []
            }
            provinceDetails[provinceName].push({
              name: item.name,  // 原始城市名
              count: Number(item.value)
            })
          }
        }
      })
      
      // 对每个省份的城市按数量降序排序
      Object.keys(provinceDetails).forEach(province => {
        provinceDetails[province].sort((a, b) => b.count - a.count)
      })
      
      // 转换为数组格式
      cityData.value = Array.from(domesticMap, ([name, value]) => ({ name, value }))
      cityDetails.value = provinceDetails
      overseasData.value = Array.from(overseasMap, ([name, value]) => ({ name, value }))
        .sort((a, b) => b.value - a.value) // 按数量降序
      
      // 调试输出
      console.log('原始城市数据:', cityRes.data.data)
      console.log('国内省份数据:', cityData.value)
      console.log('省份城市明细:', cityDetails.value)
      console.log('海外城市数据:', overseasData.value)
    }

    // 加载热门视频
    const topRes = await adminAPI.getTopVideos(10)
    if (topRes.data.success) {
      topVideos.value = topRes.data.data
    }
  } catch (error) {
    console.error('加载Dashboard数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadDashboardData()
}

// 加载中国地图数据
const loadChinaMap = async () => {
  try {
    // 从阿里云DataV获取中国地图GeoJSON数据
    const response = await fetch('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json')
    chinaMapData = await response.json()
    
    // 注册地图
    echarts.registerMap('china', chinaMapData)
    console.log('中国地图注册成功')
    
    // 调试：输出地图中的所有区域名称
    const regions = chinaMapData.features.map(f => f.properties.name)
    console.log('地图支持的区域名称:', regions)
    console.log('提示：请确保数据库中的城市名称与以上列表匹配')
    
    // 标记地图准备就绪
    isMapReady.value = true
  } catch (error) {
    console.error('加载中国地图失败:', error)
    ElMessage.warning('地图数据加载失败，城市分布图将无法显示')
  }
}

onMounted(async () => {
  // 先加载地图数据
  await loadChinaMap()
  // 再加载Dashboard数据
  loadDashboardData()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3, .card-header h4 {
  margin: 0;
}

.stats-row {
  margin-top: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  margin-right: 15px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.chart {
  height: 350px;
}

.map-container-wrapper {
  position: relative;
  width: 100%;
  height: 800px;
}

.chart-large {
  width: 100%;
  height: 800px;
}

.bay-area-inset {
  position: absolute;
  right: 20px;
  bottom: 20px;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid #409EFF;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 100;
}

.bay-area-chart {
  width: 100%;
  height: 100%;
}

.chart-small {
  height: 200px;
}

.top-videos-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.video-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: #fff;
  border-radius: 8px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.video-item:hover {
  background-color: #f5f7fa;
  transform: translateX(5px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.rank {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 18px;
  font-style: italic;
  color: #999;
  margin-right: 15px;
}

.rank-1 {
  color: #fff;
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  border-radius: 50%;
  font-size: 22px;
  box-shadow: 0 4px 8px rgba(255, 215, 0, 0.3);
}

.rank-2 {
  color: #fff;
  background: linear-gradient(135deg, #c0c0c0 0%, #d8d8d8 100%);
  border-radius: 50%;
  font-size: 20px;
  box-shadow: 0 4px 8px rgba(192, 192, 192, 0.3);
}

.rank-3 {
  color: #fff;
  background: linear-gradient(135deg, #cd7f32 0%, #d4a574 100%);
  border-radius: 50%;
  font-size: 20px;
  box-shadow: 0 4px 8px rgba(205, 127, 50, 0.3);
}

.video-thumbnail {
  width: 160px;
  height: 90px;
  aspect-ratio: 16 / 9;
  object-fit: cover;
  border-radius: 6px;
  margin-right: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.video-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
  height: 90px;
}

.video-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  overflow: hidden;
  line-height: 1.4;
}

.video-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}

.video-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.loading-placeholder {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}

.loading-placeholder-large {
  height:800px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}

.loading-placeholder .el-icon,
.loading-placeholder-large .el-icon {
  font-size: 24px;
  margin-bottom: 10px;
}
</style>
