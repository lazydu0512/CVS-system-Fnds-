/**
 * 媒体URL处理工具
 * 统一处理本地和OSS的视频/图片URL
 */

// OSS域名
const OSS_DOMAIN = 'https://cvs-system.oss-cn-guangzhou.aliyuncs.com'

// 本地后端地址
const LOCAL_BACKEND = 'http://localhost:8080'

/**
 * 获取完整的媒体URL
 * 处理三种情况：
 * 1. 已经是完整URL (http/https开头) - 直接返回
 * 2. blob: URL (本地预览) - 直接返回
 * 3. 相对路径 (/uploads/...) - 添加本地后端前缀
 * 
 * @param {string} url - 原始URL
 * @returns {string} 完整URL
 */
export function getMediaUrl(url) {
    if (!url) return ''

    // 已经是完整URL或blob预览URL，直接返回
    if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('blob:')) {
        return url
    }

    // 相对路径，添加本地后端前缀
    return `${LOCAL_BACKEND}${url.startsWith('/') ? '' : '/'}${url}`
}

/**
 * 获取视频URL
 */
export function getVideoUrl(url) {
    return getMediaUrl(url)
}

/**
 * 获取封面图片URL
 */
export function getThumbnailUrl(url) {
    return getMediaUrl(url)
}

/**
 * 获取头像URL
 */
export function getAvatarUrl(url) {
    if (!url) {
        return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    }
    return getMediaUrl(url)
}

export default {
    getMediaUrl,
    getVideoUrl,
    getThumbnailUrl,
    getAvatarUrl,
    OSS_DOMAIN,
    LOCAL_BACKEND
}
