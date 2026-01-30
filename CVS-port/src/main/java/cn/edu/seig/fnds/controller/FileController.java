package cn.edu.seig.fnds.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    // 基础路径：项目根目录下的uploads目录
    private static final String BASE_PATH = System.getProperty("user.dir") +
            File.separator + "uploads";

    /**
     * 上传视频文件
     */
    @PostMapping("/upload-video")
    public ResponseEntity<Map<String, Object>> uploadVideo(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "video", new String[]{".mp4", ".avi", ".mov", ".mkv", ".flv", ".wmv"});
    }

    /**
     * 上传封面图片
     */
    @PostMapping("/upload-cover")
    public ResponseEntity<Map<String, Object>> uploadCover(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "cover", new String[]{".jpg", ".jpeg", ".png", ".gif", ".webp"});
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/upload-avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "avatar", new String[]{".jpg", ".jpeg", ".png", ".gif", ".webp"});
    }

    /**
     * 通用文件上传方法
     * @param file 上传的文件
     * @param type 文件类型（video/cover）
     * @param allowedExtensions 允许的文件扩展名
     */
    private ResponseEntity<Map<String, Object>> uploadFile(MultipartFile file, String type, String[] allowedExtensions) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            // 获取原始文件名和后缀
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                result.put("success", false);
                result.put("message", "文件名无效");
                return ResponseEntity.badRequest().body(result);
            }

            String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

            // 验证文件扩展名
            boolean isValidExtension = false;
            for (String ext : allowedExtensions) {
                if (suffix.equals(ext)) {
                    isValidExtension = true;
                    break;
                }
            }

            if (!isValidExtension) {
                result.put("success", false);
                result.put("message", "不支持的文件格式");
                return ResponseEntity.badRequest().body(result);
            }

            // 生成新文件名：时间戳_UUID.扩展名
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String newFilename = timestamp + "_" + uuid + suffix;

            // 确定存储目录
            String uploadDir = BASE_PATH + File.separator + type;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File dest = new File(uploadDir + File.separator + newFilename);
            file.transferTo(dest);

            // 返回访问URL
            String url = "http://localhost:8080/uploads/" + type + "/" + newFilename;

            result.put("success", true);
            result.put("message", "上传成功");
            result.put("url", url);
            result.put("filename", newFilename);
            result.put("originalFilename", originalFilename);
            result.put("size", file.getSize());

            System.out.println("文件上传成功: " + type + " -> " + newFilename);

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
