<template>
  <el-container class="layout-container">
    <el-aside :width="asideWidth" class="aside" :class="{ collapsed: isCollapse }">
      <div class="logo">
        <img src="/logo.svg" alt="logo" class="logo-img" />
        <span v-show="!isCollapse">校园访客管理系统</span>
        <div class="collapse-btn" @click="toggleCollapse">
          <el-icon><component :is="isCollapse ? Expand : Fold" /></el-icon>
        </div>
      </div>
      <el-menu
        :default-active="route.path"
        class="el-menu-vertical"
        router
        :collapse="isCollapse"
        :collapse-transition="false"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <template #title>📊 仪表盘</template>
        </el-menu-item>

        <el-menu-item index="/bigscreen" v-if="hasRole('ADMIN') || hasRole('GUARD')">
          <el-icon><TrendCharts /></el-icon>
          <template #title>📺 实时大屏</template>
        </el-menu-item>
        
        <el-menu-item index="/appointments" v-if="hasRole('ADMIN') || hasRole('HOST') || hasRole('VISITOR')">
          <el-icon><Calendar /></el-icon>
          <template #title>👥 访客管理</template>
        </el-menu-item>

        <el-menu-item index="/messages">
          <el-icon><Message /></el-icon>
          <template #title>💬 消息</template>
        </el-menu-item>
        
        <el-menu-item index="/wall">
          <el-icon><Message /></el-icon>
          <template #title>🧱 留言墙</template>
        </el-menu-item>

        <el-menu-item index="/feedback" v-if="hasRole('VISITOR')">
          <el-icon><Message /></el-icon>
          <template #title>⭐ 满意度评价</template>
        </el-menu-item>

        <el-menu-item index="/gate" v-if="hasRole('GUARD')">
          <el-icon><Key /></el-icon>
          <template #title>🚪 闸机控制</template>
        </el-menu-item>

        <el-menu-item index="/statistics" v-if="hasRole('ADMIN')">
          <el-icon><TrendCharts /></el-icon>
          <template #title>📈 统计分析</template>
        </el-menu-item>

        <el-menu-item index="/admin/users" v-if="hasRole('ADMIN')">
          <el-icon><User /></el-icon>
          <template #title>🧑 用户管理</template>
        </el-menu-item>

        <template v-if="hasRole('ADMIN')">
          <div class="menu-divider"></div>
          <div class="menu-group-title" v-show="!isCollapse">系统设置</div>

          <el-menu-item index="/admin/blacklist">
            <el-icon><RemoveFilled /></el-icon>
            <template #title>⚙️ 黑名单管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/config">
            <el-icon><Setting /></el-icon>
            <template #title>⚙️ 系统配置</template>
          </el-menu-item>

          <el-menu-item index="/admin/feedback">
            <el-icon><Message /></el-icon>
            <template #title>⚙️ 评价管理</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <div class="system-name">校园访客管理系统</div>
        </div>
        <div class="header-right">
          <el-popover placement="bottom-end" :width="360" trigger="click" @show="fetchNotifications">
            <template #reference>
              <div class="action-item" @click="fetchNotifications">
                <el-badge :is-dot="hasUnread" class="notification-badge">
                  <el-icon><Bell /></el-icon>
                </el-badge>
              </div>
            </template>
            <div class="notify-panel">
              <div class="notify-header">
                <div class="notify-title">通知</div>
                <el-button link type="primary" @click="fetchNotifications" :loading="notifyLoading">刷新</el-button>
              </div>

              <div class="notify-body">
                <div v-if="notifyLoading" class="notify-empty">加载中...</div>
                <div v-else-if="notifications.length === 0" class="notify-empty">暂无通知</div>
                <div v-else class="notify-list">
                  <div v-for="item in notifications" :key="item.key" class="notify-item" @click="handleNotificationClick(item)">
                    <div class="notify-item-title">{{ item.title }}</div>
                    <div v-if="item.desc" class="notify-item-desc">{{ item.desc }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-popover>
          
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-profile">
              <el-avatar :size="32" class="avatar">{{ (user?.realName || user?.username || 'U')[0].toUpperCase() }}</el-avatar>
              <span class="username">{{ user?.realName || user?.username }}</span>
              <el-icon class="el-icon--right"><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <div class="dropdown-header">
                  <p class="role-tag">{{ getRoleLabel() }}</p>
                </div>
                <el-dropdown-item command="profile" divided>个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  DataBoard, Calendar, Key, TrendCharts, 
  User, Expand, Fold, Bell, CaretBottom, Message, Setting, RemoveFilled
} from '@element-plus/icons-vue'
import { getMyMessages } from '../api/messages'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const viewportWidth = ref(1200)
const asideWidth = computed(() => {
  if (isCollapse.value) return '64px'
  if (viewportWidth.value >= 768 && viewportWidth.value < 1200) return '200px'
  return '260px'
})

type NotifyItem = {
  key: string
  title: string
  desc?: string
  isRead?: boolean
}

const notifyLoading = ref(false)
const notifications = ref<NotifyItem[]>([])
let notifyTimer: number | undefined

const user = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : null
})

const hasRole = (role: string) => {
  const roles: string[] = user.value?.roles || []
  if (role.startsWith('ROLE_')) return roles.includes(role)
  return roles.includes(`ROLE_${role}`)
}

const hasUnread = computed(() => {
  return notifications.value.some((n) => n && n.isRead === false)
})

const fetchNotifications = async () => {
  if (notifyLoading.value) return
  notifyLoading.value = true
  try {
    const res: any = await getMyMessages({ current: 1, size: 8 })
    const records: any[] = res?.data?.records || []
    notifications.value = records.map((m) => ({
      key: String(m.id),
      title: m.title,
      desc: m.content,
      isRead: m.isRead
    }))
  } finally {
    notifyLoading.value = false
  }
}

const handleNotificationClick = (item: NotifyItem) => {
  router.push('/messages')
}

const getRoleLabel = () => {
  const roles = user.value?.roles || []
  if (roles.includes('ROLE_ADMIN')) return '管理员'
  if (roles.includes('ROLE_HOST')) return '教职工'
  if (roles.includes('ROLE_GUARD')) return '安保人员'
  return '访客'
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleResize = () => {
  viewportWidth.value = window.innerWidth
  if (window.innerWidth < 768) {
    isCollapse.value = true
  }
}

onMounted(() => {
  fetchNotifications()
  notifyTimer = window.setInterval(() => {
    fetchNotifications()
  }, 60_000)
  handleResize()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (notifyTimer) window.clearInterval(notifyTimer)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  background-color: transparent;
}

.aside {
  background-color: var(--app-card);
  border-right: 1px solid var(--app-border);
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.2, 0, 0, 1) 0s;
  box-shadow: 2px 0 8px rgba(0,0,0,0.02);
  z-index: 10;

  &.collapsed {
    width: 64px;
  }

  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    font-weight: 600;
    color: var(--app-primary);
    border-bottom: 1px solid var(--app-border);
    white-space: nowrap;
    overflow: hidden;
    position: relative;
    
    .logo-img {
      width: 32px;
      height: 32px;
      margin-right: 8px;
    }
    
    span {
      transition: opacity 0.3s;
    }

    .collapse-btn {
      position: absolute;
      right: 8px;
      top: 50%;
      transform: translateY(-50%);
      width: 32px;
      height: 32px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: var(--app-text-2);
      transition: all 0.15s ease;
    }

    .collapse-btn:hover {
      background-color: var(--app-primary-bg);
      color: var(--app-primary);
    }
  }

  .el-menu-vertical {
    border-right: none;
    flex: 1;
    padding-top: 8px;
    
    :deep(.el-menu-item) {
      margin: 4px 10px;
      border-radius: 8px;
      height: 48px;
      line-height: 48px;
      
      &.is-active {
        background: var(--app-primary-bg);
        color: var(--app-primary);
        font-weight: 600;
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 4px;
          background-color: var(--app-primary);
          border-top-left-radius: 8px;
          border-bottom-left-radius: 8px;
        }
      }
      
      &:hover {
        background-color: var(--app-primary-bg);
      }
    }
  }

  .menu-divider {
    height: 1px;
    margin: 8px 12px;
    background: var(--app-border);
  }

  .menu-group-title {
    padding: 6px 16px 2px 16px;
    font-size: 12px;
    color: var(--app-text-3);
  }
  
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  background-color: var(--app-card);
  border-bottom: 1px solid var(--app-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.04);
  z-index: 9;

  .system-name {
    font-size: 18px;
    font-weight: 700;
    color: var(--app-text-1);
    white-space: nowrap;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 24px;
    
    .action-item {
      cursor: pointer;
      color: #666;
      transition: color 0.3s;
      display: flex;
      align-items: center;
      
      &:hover {
        color: var(--app-primary);
      }
      
      .el-icon {
        font-size: 20px;
      }
    }
    
    .user-profile {
      cursor: pointer;
      display: flex;
      align-items: center;
      padding: 4px 8px;
      border-radius: 4px;
      transition: background-color 0.3s;
      
      &:hover {
        background-color: var(--app-header-bg);
      }
      
      .avatar {
        background-color: var(--app-primary);
        color: white;
        margin-right: 8px;
      }
      
      .username {
        font-size: 14px;
        color: var(--app-text-1);
        margin-right: 4px;
      }
      
      .el-icon--right {
        color: var(--app-text-3);
        font-size: 12px;
      }
    }
  }
}

.notify-panel {
  display: flex;
  flex-direction: column;
}

.notify-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.notify-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f1f1f;
}

.notify-body {
  padding-top: 10px;
}

.notify-empty {
  padding: 16px 0;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.notify-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notify-item {
  padding: 10px 12px;
  border-radius: 8px;
  background: #f6f8fb;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.notify-item:hover {
  background: #eef5ff;
}

.notify-item-title {
  font-size: 13px;
  color: #1f1f1f;
  font-weight: 500;
}

.notify-item-desc {
  margin-top: 4px;
  font-size: 12px;
  color: #8c8c8c;
}

.main {
  background-color: transparent;
  padding: 24px 32px;
  overflow-y: auto;
  
  /* 滚动条样式 */
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: #ccc;
    border-radius: 3px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
}

.user-dropdown {
  .dropdown-header {
    padding: 8px 16px;
    text-align: center;
    background-color: #f9f9f9;
    margin-bottom: 4px;
    
    .role-tag {
      margin: 0;
      font-size: 12px;
      color: #1677ff;
      background: #e6f7ff;
      padding: 2px 8px;
      border-radius: 10px;
      display: inline-block;
    }
  }
}

@media (max-width: 768px) {
  .main {
    padding: 16px;
  }
}
</style>
