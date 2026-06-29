<template>
  <div class="dashboard-container">
    <el-carousel :interval="6000" arrow="always" height="600px" class="mb-20 shadow-lg">
      <el-carousel-item v-for="(img, index) in carouselImages" :key="index">
        <div class="carousel-item-container">
          <div class="carousel-image-wrapper">
            <img :src="img" class="carousel-image" loading="eager" />
          </div>
          <div class="carousel-caption">
            <div class="caption-content">
              <h3>校园风光 - {{ index + 1 }}</h3>
              <p>智慧校园访客管理系统 · 欢迎您的到来</p>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>今日访客</span>
              <el-tag type="success">今日</el-tag>
            </div>
          </template>
          <div class="card-content">
            <span class="number">12</span>
            <span class="label">人</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>待审批</span>
              <el-tag type="warning">需要处理</el-tag>
            </div>
          </template>
          <div class="card-content">
            <span class="number">5</span>
            <span class="label">条</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>当前在校</span>
              <el-tag type="info">实时</el-tag>
            </div>
          </template>
          <div class="card-content">
            <span class="number">8</span>
            <span class="label">人</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>累计来访</span>
              <el-tag>本月</el-tag>
            </div>
          </template>
          <div class="card-content">
            <span class="number">156</span>
            <span class="label">条</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>访客趋势</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <!-- ECharts will be integrated here -->
             <LineChart
              v-if="trendData.dates.length > 0"
              :x-axis-data="trendData.dates"
              :series-data="trendData.counts"
              title="每日访客数"
              height="300px"
            />
            <el-empty v-else-if="trendLoading" description="数据加载中..." />
            <el-empty v-else-if="!isAdmin" description="仅管理员可查看统计趋势" />
            <el-empty v-else description="暂无数据" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button v-if="isVisitor" type="primary" icon="Calendar" class="w-full mb-10" @click="$router.push('/appointments/create')">
              新建预约
            </el-button>
            <el-button v-if="isHost || isAdmin" type="success" icon="Check" class="w-full mb-10" @click="$router.push('/appointments')">
              审批申请
            </el-button>
            <el-button type="info" icon="Search" class="w-full" @click="$router.push('/appointments')">
              查看预约
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import LineChart from '../../components/charts/LineChart.vue'
import { getVisitorTrend } from '../../api/stats'
import dayjs from 'dayjs'

const trendData = ref({ dates: [], counts: [] })
const trendLoading = ref(false)

const carouselImages = [
  '/carousel/1.jpg',
  '/carousel/2.jpg',
  '/carousel/3.jpg',
  '/carousel/4.png',
  '/carousel/5.jpg'
]

const user = JSON.parse(localStorage.getItem('user') || 'null')
const roles: string[] = user?.roles || []
const isAdmin = computed(() => roles.includes('ROLE_ADMIN'))
const isHost = computed(() => roles.includes('ROLE_HOST'))
const isVisitor = computed(() => roles.includes('ROLE_VISITOR'))

const fetchTrend = async () => {
  trendLoading.value = true
  const endDate = dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss')
  const startDate = dayjs().subtract(7, 'day').startOf('day').format('YYYY-MM-DD HH:mm:ss')
  
  try {
    const res: any = await getVisitorTrend({ startDate, endDate })
    trendData.value = {
      dates: res.data.map((item: any) => item.date),
      counts: res.data.map((item: any) => item.count)
    }
  } catch (e) {
    console.error('Failed to fetch dashboard trend', e)
  } finally {
    trendLoading.value = false
  }
}

onMounted(() => {
  if (isAdmin.value) fetchTrend()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  .mb-20 {
    margin-bottom: 20px;
  }
  
  .shadow-lg {
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    border-radius: 16px;
    overflow: hidden;
  }

  .carousel-item-container {
    position: relative;
    width: 100%;
    height: 100%;
    
    .carousel-image-wrapper {
      width: 100%;
      height: 100%;
      overflow: hidden;
      
      .carousel-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        object-position: center;
        transition: transform 12s ease;
      }
    }

    /* 激活状态下的缩放效果，让静态图有动态感，视觉上更清晰 */
    &.is-active .carousel-image {
      transform: scale(1.1);
    }
    
    .carousel-caption {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 60px 30px 30px;
      background: linear-gradient(to top, rgba(0, 0, 0, 0.8) 0%, rgba(0, 0, 0, 0.4) 60%, transparent 100%);
      color: white;
      
      .caption-content {
        max-width: 800px;
        margin: 0;
        
        h3 {
          margin: 0 0 12px;
          font-size: 32px;
          font-weight: 700;
          letter-spacing: 1px;
          text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
        }
        
        p {
          margin: 0;
          font-size: 18px;
          opacity: 0.9;
          font-weight: 300;
          letter-spacing: 0.5px;
          text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
        }
      }
    }
  }

  /* Element Plus 内部类，用于触发缩放 */
  :deep(.el-carousel__item.is-active) .carousel-image {
    transform: scale(1.15);
  }

  .stat-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .card-content {
      display: flex;
      align-items: baseline;
      .number {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
        margin-right: 8px;
      }
      .label {
        font-size: 14px;
        color: #909399;
      }
    }
  }
  
  .mt-20 {
    margin-top: 20px;
  }
  
  .chart-placeholder {
    height: 300px;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f5f7fa;
  }
  
  .quick-actions {
    .w-full {
      width: 100%;
    }
    .mb-10 {
      margin-bottom: 10px;
    }
  }
}
</style>
