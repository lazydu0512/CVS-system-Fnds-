package cn.edu.seig.fnds.controller;

import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.entity.VideoCollect;
import cn.edu.seig.fnds.service.VideoCollectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频收藏控制器
 * @author lazzydu
 */
@RestController
@RequestMapping("/api/video-collect")
@CrossOrigin
public class VideoCollectController {

    private final VideoCollectService videoCollectService;

    public VideoCollectController(VideoCollectService videoCollectService) {
        this.videoCollectService = videoCollectService;
    }

    /**
     * 收藏/取消收藏
     */
    @PostMapping("/{videoId}")
    public ResponseEntity<Map<String, Object>> toggleCollect(@PathVariable Long videoId,
                                                            @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = videoCollectService.toggleCollect(videoId, user.getId());
            if (success) {
                boolean isCollected = videoCollectService.isCollected(videoId, user.getId());
                result.put("success", true);
                result.put("message", isCollected ? "收藏成功" : "取消收藏成功");
                result.put("collected", isCollected);
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "操作失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/{videoId}/status")
    public ResponseEntity<Map<String, Object>> checkCollectStatus(@PathVariable Long videoId,
                                                                 @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean isCollected = videoCollectService.isCollected(videoId, user.getId());
            result.put("success", true);
            result.put("collected", isCollected);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserCollects(@RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<VideoCollect> collects = videoCollectService.getUserCollects(user.getId());
            result.put("success", true);
            result.put("data", collects);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}