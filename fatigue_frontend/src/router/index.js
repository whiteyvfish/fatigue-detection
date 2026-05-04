import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'
import ImageDetect from '@/views/ImageDetect.vue'
import RealtimeDetect from '@/views/RealtimeDetect.vue'
import History from '@/views/History.vue'
import Statistics from '@/views/Statistics.vue'
import VideoFatigueDetection from '@/views/VideoFatigueDetection.vue'
const routes = [
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
                meta: { title: '视频检测', icon: 'Film' }   // 或 'VideoCamera'
            },
            {
                path: '/realtime',
                name: 'RealtimeDetect',
                component: RealtimeDetect,
                meta: { title: '实时监测', icon: 'VideoCamera' }
            },
            /*{
                path: '/video-detection',
                name: 'VideoFatigueDetection',
                component: VideoFatigueDetection
            },*/
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

export default router