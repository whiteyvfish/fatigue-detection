import axios from 'axios'

const api = axios.create({
    baseURL: '/api',
    timeout: 60000
    // 注意：不要在这里全局设置 Content-Type: multipart/form-data
    // 上传文件时浏览器会自动添加正确的 boundary，手动设置反而会导致后端解析失败
})

// ==================== 图片检测 ====================

/**
 * 图片检测 - 完整流程（后端保存记录 + 可选可视化）
 * @param {File} file - 图片文件
 * @param {string} userId - 用户ID
 * @param {boolean} visualize - 是否返回标注结果图（base64）
 */
export const detectImage = (file, userId = 'anonymous', visualize = true) => {
    const formData = new FormData()
    formData.append('image', file)
    formData.append('userId', userId)
    formData.append('visualize', visualize)
    return api.post('/detection/image', formData)
}

/**
 * 快速检测 - 不保存记录，直接返回AI结果
 * @param {File} file - 图片文件
 */
export const quickDetect = (file) => {
    const formData = new FormData()
    formData.append('image', file)
    return api.post('/detection/quick', formData)
}

// ==================== 历史记录 ====================

export const getHistory = (userId, page = 0, size = 10) => {
    return api.get('/detection/history', {
        params: { userId, page, size }
    })
}

export const getAllHistory = (userId) => {
    return api.get('/detection/history/all', {
        params: { userId }
    })
}

export const getRecord = (recordId) => {
    return api.get(`/detection/record/${recordId}`)
}

// ==================== 统计分析 ====================

export const getStatistics = (userId, days = 7) => {
    return api.get('/detection/statistics', {
        params: { userId, days }
    })
}

// ==================== 健康检查 ====================

export const healthCheck = () => {
    return api.get('/detection/health')
}

// ==================== 视频检测 ====================

/**
 * 视频疲劳分析（上传视频 → AI分析 → 返回报告）
 * @param {File} video - MP4 视频文件
 * @param {string} userId - 用户ID
 * @param {number} sampleRate - 采样率（每隔N帧检测一次）
 */
export const detectVideo = (video, userId = 'anonymous', sampleRate = 1) => {
    const formData = new FormData()
    formData.append('video', video)
    formData.append('userId', userId)
    formData.append('sampleRate', sampleRate)
    return api.post('/detection/video', formData)
}

// ==================== 实时帧检测 ====================

/**
 * 单帧检测（用于实时视频流）
 * @param {string} base64Image - Base64 编码的帧图像（不含前缀）
 */
export const detectFrame = (base64Image) => {
    return api.post('/detection/frame', {
        image_base64: base64Image
    }, {
        headers: { 'Content-Type': 'application/json' }
    })
}

// ==================== WebSocket 实时检测 ====================

export class DetectionWebSocket {
    constructor(onMessage, onError) {
        this.ws = null
        this.onMessage = onMessage
        this.onError = onError
        this.frameCount = 0
    }

    connect() {
        // 【关键】直连后端 8080，彻底绕过 Vite 代理
        const wsUrl = 'ws://localhost:8080/api/ws/detection'
        console.log('[WS] 连接地址:', wsUrl)

        this.ws = new WebSocket(wsUrl)

        this.ws.onopen = () => {
            console.log('[WS] 已连接')
            if (this.onMessage) {
                this.onMessage({ type: 'connected', message: '实时检测服务已连接' })
            }
        }

        this.ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data)
                if (this.onMessage) this.onMessage(data)
            } catch (e) {
                console.error('[WS] 消息解析失败:', e)
            }
        }

        this.ws.onerror = (error) => {
            console.error('[WS] 错误:', error)
            if (this.onError) this.onError(error)
        }

        this.ws.onclose = () => {
            console.log('[WS] 连接关闭')
            if (this.onMessage) {
                this.onMessage({ type: 'disconnected', message: '连接已断开' })
            }
        }
    }

    sendFrame(frameData) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify({
                data: frameData.imageBase64,
                frameIndex: frameData.frameIndex,
                timestamp: Date.now()
            }))
        }
    }

    disconnect() {
        if (this.ws) {
            this.ws.close()
            this.ws = null
        }
    }
}

// ==================== 视频异步检测（新增） ====================

/**
 * 1. 上传视频，启动后台异步分析
 * @param {File} file - MP4 文件
 * @param {string} userId - 用户ID
 */
export const uploadVideo = (file, userId = 'anonymous') => {
    const formData = new FormData()
    formData.append('file', file)   // 【注意】后端参数名是 file，不是 video
    formData.append('userId', userId)
    return api.post('/video/upload', formData)
}

/**
 * 2. 轮询任务状态（processing / completed / failed）
 * @param {string} videoId
 */
export const getVideoStatus = (videoId) => {
    return api.get(`/video/${videoId}`)
}

/**
 * 3. 查询任务详情（完成后调用，含视频 URL、统计字段）
 * @param {string} videoId
 */
export const getVideoDetail = (videoId) => {
    return api.get(`/video/${videoId}/detail`)
}

export default api
