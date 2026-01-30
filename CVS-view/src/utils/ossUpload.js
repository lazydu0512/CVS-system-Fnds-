/**
 * 阿里云OSS上传工具
 * 支持前端直传到OSS
 */
import OSS from 'ali-oss'
import axios from 'axios'

// 获取上传凭证
async function getUploadToken(type) {
    const token = localStorage.getItem('token')
    const response = await axios.get(`/api/oss/upload-token/${type}`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {}
    })

    if (response.data.success) {
        return response.data.data
    }
    throw new Error(response.data.message || '获取上传凭证失败')
}

// 创建OSS客户端
function createOSSClient(credentials) {
    return new OSS({
        region: credentials.region,
        accessKeyId: credentials.accessKeyId,
        accessKeySecret: credentials.accessKeySecret,
        stsToken: credentials.securityToken,
        bucket: credentials.bucket,
        secure: true
    })
}

// 获取文件扩展名
function getFileExtension(filename) {
    const lastDot = filename.lastIndexOf('.')
    return lastDot > 0 ? filename.substring(lastDot).toLowerCase() : ''
}

/**
 * 上传视频文件到OSS
 * @param {File} file 视频文件
 * @param {Function} onProgress 进度回调 (percent: number) => void
 * @returns {Promise<{url: string, objectKey: string}>}
 */
export async function uploadVideoToOSS(file, onProgress) {
    try {
        // 获取上传凭证
        const credentials = await getUploadToken('video')

        // 创建OSS客户端
        const client = createOSSClient(credentials)

        // 生成完整的objectKey（添加文件扩展名）
        const extension = getFileExtension(file.name)
        const objectKey = credentials.objectKey + extension

        // 使用分片上传（适合大文件）
        const result = await client.multipartUpload(objectKey, file, {
            progress: (p) => {
                if (onProgress) {
                    onProgress(Math.round(p * 100))
                }
            },
            parallel: 4, // 并行上传数
            partSize: 1024 * 1024 * 5 // 每片5MB
        })

        // 构造访问URL
        const url = credentials.cdnDomain
            ? `${credentials.cdnDomain}/${objectKey}`.replace(/([^:])\/\//g, '$1/')
            : `https://${credentials.bucket}.${credentials.endpoint}/${objectKey}`

        return {
            url,
            objectKey,
            filename: file.name,
            size: file.size
        }
    } catch (error) {
        console.error('OSS上传失败:', error)
        throw error
    }
}

/**
 * 上传封面图片到OSS
 * @param {File} file 图片文件
 * @param {Function} onProgress 进度回调
 * @returns {Promise<{url: string, objectKey: string}>}
 */
export async function uploadCoverToOSS(file, onProgress) {
    try {
        const credentials = await getUploadToken('cover')
        const client = createOSSClient(credentials)

        const extension = getFileExtension(file.name)
        const objectKey = credentials.objectKey + extension

        // 封面图片较小，使用简单上传
        const result = await client.put(objectKey, file)

        if (onProgress) {
            onProgress(100)
        }

        const url = credentials.cdnDomain
            ? `${credentials.cdnDomain}/${objectKey}`.replace(/([^:])\/\//g, '$1/')
            : `https://${credentials.bucket}.${credentials.endpoint}/${objectKey}`

        return {
            url,
            objectKey,
            filename: file.name,
            size: file.size
        }
    } catch (error) {
        console.error('OSS上传封面失败:', error)
        throw error
    }
}

/**
 * 上传头像到OSS
 * @param {File} file 头像文件
 * @param {Function} onProgress 进度回调
 * @returns {Promise<{url: string, objectKey: string}>}
 */
export async function uploadAvatarToOSS(file, onProgress) {
    try {
        const credentials = await getUploadToken('avatar')
        const client = createOSSClient(credentials)

        const extension = getFileExtension(file.name)
        const objectKey = credentials.objectKey + extension

        const result = await client.put(objectKey, file)

        if (onProgress) {
            onProgress(100)
        }

        const url = credentials.cdnDomain
            ? `${credentials.cdnDomain}/${objectKey}`.replace(/([^:])\/\//g, '$1/')
            : `https://${credentials.bucket}.${credentials.endpoint}/${objectKey}`

        return {
            url,
            objectKey,
            filename: file.name,
            size: file.size
        }
    } catch (error) {
        console.error('OSS上传头像失败:', error)
        throw error
    }
}

export default {
    uploadVideoToOSS,
    uploadCoverToOSS,
    uploadAvatarToOSS
}
