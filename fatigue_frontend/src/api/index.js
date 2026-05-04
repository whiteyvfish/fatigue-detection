import axios from 'axios'


const api = axios.create({
    baseURL: '/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'multipart/form-data'
    }
})

// 图片检测
export const detectImage = (file, userId = 'anonymous') => {
    const formData = new FormData()
    formData.append('image', file)
    formData.append('userId', userId)
    return api.post('/detection/image', formData)
}

// 快速检测
export const quickDetect = (file) => {
    const formData = new FormData()
    formData.append('image', file)
    return api.post('/detection/quick', formData)
}

// 获取历史记录
export const getHistory = (userId, page = 0, size = 10) => {
    return api.get('/detection/history', {
        params: { userId, page, size }
    })
}

// 获取统计信息
export const getStatistics = (userId, days = 7) => {
    return api.get('/detection/statistics', {
        params: { userId, days }
    })
}

// 获取记录详情
export const getRecord = (recordId) => {
    return api.get(`/detection/record/${recordId}`)
}

// 健康检查
export const healthCheck = () => {
    return api.get('/detection/health')
}

// WebSocket连接（实时检测）
export class DetectionWebSocket {
    constructor(onMessage, onError) {
        this.ws = null
        this.onMessage = onMessage
        this.onError = onError
        this.frameCount = 0
    }

    connect() {
        this.ws = new WebSocket('ws://localhost:8080/api/ws/detection')

        this.ws.onopen = () => {
            console.log('WebSocket已连接')
            this.onMessage({ type: 'connected', message: '实时检测服务已连接' })
        }

        this.ws.onmessage = (event) => {
            const data = JSON.parse(event.data)
            this.onMessage(data)
        }

        this.ws.onerror = (error) => {
            console.error('WebSocket错误:', error)
            this.onError(error)
        }

        this.ws.onclose = () => {
            console.log('WebSocket连接关闭')
            this.onMessage({ type: 'disconnected', message: '连接已断开' })
        }
    }


    sendFrame(frameData) {  // 参数改为对象
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify({
                data: frameData.imageBase64,  // 提取base64
                frameIndex: frameData.frameIndex,
                timestamp: Date.now()
            }))
        }
    }

    disconnect() {
        if (this.ws) {
            this.ws.close()
        }
    }
}

export default api