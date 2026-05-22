import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'
import ImageDetect from '@/views/ImageDetect.vue'
import RealtimeDetect from '@/views/RealtimeDetect.vue'
import History from '@/views/History.vue'
import Statistics from '@/views/Statistics.vue'
import VideoFatigueDetection from '@/views/VideoFatigueDetection.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: Login,
        meta: { title: '登录', public: true }
    },
    {
        path: '/register',
        name: 'Register',
        component: Register,
        meta: { title: '注册', public: true }
    },
    {
        path: '/',
        component: Layout,
        redirect: '/detect',
        children: [
            {
                path: '/detect',
                name: 'ImageDetect',
                component: ImageDetect,
                meta: { title: '图片检测', icon: 'Picture' }
            },
            {
                path: '/video',
                name: 'VideoFatigueDetection',
                component: VideoFatigueDetection,
                meta: { title: '视频检测', icon: 'Film' }
            },
            {
                path: '/realtime',
                name: 'RealtimeDetect',
                component: RealtimeDetect,
                meta: { title: '实时监测', icon: 'VideoCamera' }
            },
            {
                path: '/history',
                name: 'History',
                component: History,
                meta: { title: '检测记录', icon: 'List' }
            },
            {
                path: '/statistics',
                name: 'Statistics',
                component: Statistics,
                meta: { title: '统计分析', icon: 'TrendCharts' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫：未登录时跳转到登录页
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    // 公开页面无需登录
    if (to.meta.public) {
        return next()
    }
    // 需要登录但无 token → 跳转登录页
    if (!token) {
        return next('/login')
    }
    next()
})

export default router
