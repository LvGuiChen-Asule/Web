import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/auth/ForgotPassword.vue'),
    meta: { title: '找回密码' }
  },
  {
    path: '/',
    redirect: '/dashboard',
    component: () => import('../layouts/DefaultLayout.vue'),
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Dashboard.vue'),
        meta: { requiresAuth: true, title: '仪表盘' }
      },
      {
        path: '/bigscreen',
        name: 'BigScreen',
        component: () => import('../views/bigscreen/Overview.vue'),
        meta: { requiresAuth: true, roles: ['ROLE_ADMIN', 'ROLE_GUARD'], title: '实时大屏' }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('../views/profile/Profile.vue'),
        meta: { requiresAuth: true, title: '个人中心' }
      },
      {
        path: '/messages',
        name: 'Messages',
        component: () => import('../views/messages/List.vue'),
        meta: { requiresAuth: true, title: '消息' }
      },
      {
        path: '/wall',
        name: 'FeedbackWall',
        component: () => import('../views/feedback/Wall.vue'),
        meta: { requiresAuth: true, title: '留言墙' }
      },
      {
        path: '/feedback',
        name: 'FeedbackSubmit',
        component: () => import('../views/feedback/Submit.vue'),
        meta: { requiresAuth: true, roles: ['ROLE_VISITOR'], title: '满意度评价' }
      },
      {
        path: '/appointments',
        name: 'AppointmentList',
        component: () => import('../views/appointments/List.vue'),
        meta: { requiresAuth: true, roles: ['ROLE_ADMIN', 'ROLE_HOST', 'ROLE_VISITOR'], title: '预约管理' }
      },
      {
        path: '/appointments/create',
        name: 'CreateAppointment',
        component: () => import('../views/appointments/Create.vue'),
        meta: { requiresAuth: true, roles: ['ROLE_VISITOR'], title: '新建预约' }
      },
      {
        path: '/appointments/calendar',
        name: 'AppointmentCalendar',
        component: () => import('../views/appointments/Calendar.vue'),
        meta: { requiresAuth: true, roles: ['ROLE_ADMIN', 'ROLE_HOST', 'ROLE_VISITOR'], title: '预约日历' }
      },
      {
        path: '/gate',
        name: 'GateControl',
        component: () => import('../views/gate/Control.vue'),
        meta: { requiresAuth: true, roles: ['ROLE_GUARD'], title: '闸机控制' }
      },
      {
        path: '/statistics',
        name: 'Statistics',
        component: () => import('../views/statistics/Overview.vue'),
        meta: { requiresAuth: true, roles: ['ROLE_ADMIN'], title: '统计分析' }
      },
      {
        path: '/admin/users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue'),
        meta: { requiresAuth: true, roles: ['ROLE_ADMIN'], title: '用户管理' }
      },
      {
        path: '/admin/blacklist',
        name: 'AdminBlacklist',
        component: () => import('../views/admin/Blacklist.vue'),
        meta: { requiresAuth: true, roles: ['ROLE_ADMIN'], title: '黑名单管理' }
      },
      {
        path: '/admin/config',
        name: 'AdminConfig',
        component: () => import('../views/admin/Config.vue'),
        meta: { requiresAuth: true, roles: ['ROLE_ADMIN'], title: '系统配置' }
      },
      {
        path: '/admin/feedback',
        name: 'AdminFeedback',
        component: () => import('../views/admin/Feedback.vue'),
        meta: { requiresAuth: true, roles: ['ROLE_ADMIN'], title: '评价管理' }
      }
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null
  const roles: string[] = user?.roles || []

  if (token && (to.path === '/login' || to.path === '/register' || to.path === '/forgot-password')) {
    next('/dashboard')
    return
  }

  if (to.meta?.requiresAuth && !token) {
    next('/login')
    return
  }

  const requiredRoles = (to.meta?.roles as string[] | undefined) || []
  if (requiredRoles.length > 0) {
    const allowed = requiredRoles.some((r) => roles.includes(r))
    if (!allowed) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
