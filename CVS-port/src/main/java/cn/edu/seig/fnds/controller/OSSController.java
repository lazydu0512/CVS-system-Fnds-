package cn.edu.seig.fnds.controller;

import cn.edu.seig.fnds.service.OSSService;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * OSS上传控制器
 * 提供前端直传所需的临时凭证
 * 
 * @author lazzydu
 */
@RestController
@RequestMapping("/api/oss")
@CrossOrigin
public class OSSController {

    private final OSSService ossService;

    public OSSController(OSSService ossService) {
        this.ossService = ossService;
    }

    /**
     * 获取视频上传凭证
     */
    @GetMapping("/upload-token/video")
    public ResponseEntity<Map<String, Object>> getVideoUploadToken() {
        return getUploadToken("video");
    }

    /**
     * 获取封面上传凭证
     */
    @GetMapping("/upload-token/cover")
    public ResponseEntity<Map<String, Object>> getCoverUploadToken() {
        return getUploadToken("cover");
    }

    /**
     * 获取头像上传凭证
     */
    @GetMapping("/upload-token/avatar")
    public ResponseEntity<Map<String, Object>> getAvatarUploadToken() {
        return getUploadToken("avatar");
    }

    /**
     * 通用获取上传凭证方法
     */
    private ResponseEntity<Map<String, Object>> getUploadToken(String type) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 尝试使用STS获取临时凭证
            Map<String, Object> tokenInfo = ossService.getStsToken(type);
            result.put("success", true);
            result.put("data", tokenInfo);
            return ResponseEntity.ok(result);
        } catch (ClientException e) {
            // STS获取失败，尝试返回直接配置（仅用于开发测试）
            try {
                Map<String, Object> config = ossService.getUploadConfig(type);
                result.put("success", true);
                result.put("data", config);
                result.put("warning", "使用直接配置，建议生产环境配置STS");
                return ResponseEntity.ok(result);
            } catch (Exception ex) {
                result.put("success", false);
                result.put("message", "获取上传凭证失败: " + e.getMessage());
                return ResponseEntity.badRequest().body(result);
            }
        }
    }

    /**
     * 通知后端文件上传完成（可选，用于记录日志等）
     */
    @PostMapping("/upload-complete")
    public ResponseEntity<Map<String, Object>> uploadComplete(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        String objectKey = request.get("objectKey");
        String type = request.get("type");

        // 获取完整的访问URL
        String fileUrl = ossService.getFileUrl(objectKey);

        result.put("success", true);
        result.put("url", fileUrl);
        result.put("message", "上传成功");

        System.out.println("OSS文件上传完成: " + type + " -> " + objectKey);

        return ResponseEntity.ok(result);
    }
}
