package cn.edu.seig.fnds.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 * 
 * @author lazzydu
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OSSConfig {

    /**
     * OSS服务端点
     */
    private String endpoint;

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 区域
     */
    private String region;

    /**
     * CDN域名
     */
    private String cdnDomain;

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * RAM角色ARN (用于STS临时凭证)
     */
    private String roleArn;

    /**
     * 角色会话名称
     */
    private String roleSessionName;

    /**
     * 获取文件访问URL前缀
     * 优先使用CDN域名
     */
    public String getUrlPrefix() {
        if (cdnDomain != null && !cdnDomain.isEmpty()) {
            return cdnDomain.endsWith("/") ? cdnDomain : cdnDomain + "/";
        }
        return "https://" + bucketName + "." + endpoint + "/";
    }
}
