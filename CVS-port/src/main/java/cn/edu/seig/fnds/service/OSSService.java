package cn.edu.seig.fnds.service;

import cn.edu.seig.fnds.config.OSSConfig;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 阿里云OSS服务类
 * 提供STS临时凭证获取，用于前端直传
 * 
 * @author lazzydu
 */
@Service
public class OSSService {

    private final OSSConfig ossConfig;

    public OSSService(OSSConfig ossConfig) {
        this.ossConfig = ossConfig;
    }

    /**
     * 获取前端直传所需的STS临时凭证
     * 
     * @param type 文件类型 (video/cover/avatar)
     * @return 包含STS凭证和上传参数的Map
     */
    public Map<String, Object> getStsToken(String type) throws ClientException {
        // 创建STS客户端
        DefaultProfile profile = DefaultProfile.getProfile(
                ossConfig.getRegion(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);

        // 创建AssumeRole请求
        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setRoleArn(ossConfig.getRoleArn());
        request.setRoleSessionName(ossConfig.getRoleSessionName());
        request.setDurationSeconds(3600L); // 凭证有效期1小时

        // 发送请求获取临时凭证
        AssumeRoleResponse response = client.getAcsResponse(request);
        AssumeRoleResponse.Credentials credentials = response.getCredentials();

        // 生成文件存储路径
        String objectKey = generateObjectKey(type);

        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("accessKeyId", credentials.getAccessKeyId());
        result.put("accessKeySecret", credentials.getAccessKeySecret());
        result.put("securityToken", credentials.getSecurityToken());
        result.put("expiration", credentials.getExpiration());
        result.put("region", "oss-" + ossConfig.getRegion());
        result.put("bucket", ossConfig.getBucketName());
        result.put("objectKey", objectKey);
        result.put("endpoint", ossConfig.getEndpoint());
        result.put("cdnDomain", ossConfig.getCdnDomain());

        return result;
    }

    /**
     * 生成简单的上传凭证（不使用STS，直接返回配置信息）
     * 适用于没有配置RAM角色的情况
     * 
     * @param type 文件类型 (video/cover/avatar)
     * @return 上传配置信息
     */
    public Map<String, Object> getUploadConfig(String type) {
        String objectKey = generateObjectKey(type);

        Map<String, Object> result = new HashMap<>();
        result.put("region", "oss-" + ossConfig.getRegion());
        result.put("bucket", ossConfig.getBucketName());
        result.put("objectKey", objectKey);
        result.put("endpoint", ossConfig.getEndpoint());
        result.put("cdnDomain", ossConfig.getCdnDomain());
        // 注意：直接返回AccessKey用于前端直传存在安全风险
        // 生产环境强烈建议使用STS临时凭证
        result.put("accessKeyId", ossConfig.getAccessKeyId());
        result.put("accessKeySecret", ossConfig.getAccessKeySecret());

        return result;
    }

    /**
     * 生成OSS对象存储路径
     * 格式：{type}/{年月日}/{时间戳}_{UUID}.{扩展名占位}
     */
    private String generateObjectKey(String type) {
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String timestamp = new SimpleDateFormat("HHmmss").format(new Date());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        // 返回不带扩展名的路径，前端会自动添加扩展名
        return type + "/" + datePath + "/" + timestamp + "_" + uuid;
    }

    /**
     * 根据对象路径获取完整的访问URL
     */
    public String getFileUrl(String objectKey) {
        return ossConfig.getUrlPrefix() + objectKey;
    }

    /**
     * 获取OSS配置信息
     */
    /**
     * 删除OSS文件
     *
     * @param fileUrl 文件完整URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // 解析ObjectKey
            String objectKey = getObjectKeyFromUrl(fileUrl);
            if (objectKey == null) {
                System.out.println("ObjectKey为空，跳过删除: " + fileUrl);
                return;
            }
            System.out.println("准备删除OSS文件, Key: " + objectKey);

            // 创建Client - IAcsClient主要用于管控类操作，这里不需要
            // DefaultProfile profile = DefaultProfile.getProfile(
            // ossConfig.getRegion(),
            // ossConfig.getAccessKeyId(),
            // ossConfig.getAccessKeySecret());
            // IAcsClient client = new DefaultAcsClient(profile);

            // 删除文件 - 修正：阿里云Java SDK核心库(aliyun-java-sdk-core)通用的IAcsClient通常用于管理类操作(STS等)
            // 对于OSS的数据操作(上传/下载/删除)，官方推荐使用 aliyun-sdk-oss
            // 这里我们需要检查依赖。之前pom.xml中引入了 aliyun-sdk-oss

            // 由于项目结构中已经引入了aliyun-sdk-oss，我们应该使用OSSClient
            // 但为了保持一致性（前面getStsToken使用了IAcsClient），且避免大规模重构
            // 我们这里先使用OSSClient，因为IAcsClient主要用于OpenAPI (管控层)

            // 修正：我们需要引入OSSClient
            com.aliyun.oss.OSS ossClient = new com.aliyun.oss.OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret());

            try {
                ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
            } finally {
                ossClient.shutdown();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("删除OSS文件异常: " + e.getMessage());
            // 删除失败不抛出异常，避免影响主业务
        }
    }

    /**
     * 从URL中解析ObjectKey
     */
    private String getObjectKeyFromUrl(String url) {
        System.out.println("尝试解析OSS URL: " + url);
        try {
            // 处理本地文件 (不删除)
            if (url.startsWith("http://localhost") || url.startsWith("/uploads")) {
                System.out.println("跳过本地文件: " + url);
                return null;
            }

            // 处理CDN域名
            String cdnDomain = ossConfig.getCdnDomain();
            if (cdnDomain != null && !cdnDomain.isEmpty() && url.startsWith(cdnDomain)) {
                String key = url.substring(cdnDomain.length() + 1); // +1 去掉斜杠
                if (key.contains("?")) {
                    key = key.substring(0, key.indexOf("?"));
                }
                System.out.println("通过CDN域名解析出Key: " + key);
                return key;
            }

            // 处理直接Endpoint域名
            // 格式: https://{bucket}.{endpoint}/{objectKey}
            String endpointObj = "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint();
            if (url.startsWith(endpointObj)) {
                String key = url.substring(endpointObj.length() + 1);
                if (key.contains("?")) {
                    key = key.substring(0, key.indexOf("?"));
                }
                System.out.println("通过Endpoint域名解析出Key: " + key);
                return key;
            }

            // 如果是完整URL但没匹配上配置的域名，尝试简单的截取（假设标准格式）
            // 这是一个兜底策略
            if (url.startsWith("http")) {
                int pathIndex = url.indexOf("/", url.indexOf("://") + 3);
                if (pathIndex != -1) {
                    String key = url.substring(pathIndex + 1);
                    if (key.contains("?")) {
                        key = key.substring(0, key.indexOf("?"));
                    }
                    System.out.println("通过通用规则解析出Key: " + key);
                    return key;
                }
            }

            System.out.println("未能解析出ObjectKey: " + url);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public OSSConfig getOssConfig() {
        return ossConfig;
    }
}
