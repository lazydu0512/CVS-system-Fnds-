package cn.edu.seig.fnds.controller;

import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.service.VideoLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 视频点赞控制器
 * @author lazzydu
 */
@RestController
@RequestMapping("/api/video-like")
@CrossOrigin
public class VideoLikeController {

    private final VideoLikeService videoLikeService;

    public VideoLikeController(VideoLikeService videoLikeService) {
        this.videoLikeService = videoLikeService;
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/{videoId}")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long videoId,
                                                         @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = videoLikeService.toggleLike(videoId, user.getId());
            if (success) {
                boolean isLiked = videoLikeService.isLiked(videoId, user.getId());
                result.put("success", true);
                result.put("message", isLiked ? "点赞成功" : "取消点赞成功");
                result.put("liked", isLiked);
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
     * 检查是否已点赞
     */
    @GetMapping("/{videoId}/status")
    public ResponseEntity<Map<String, Object>> checkLikeStatus(@PathVariable Long videoId,
                                                              @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean isLiked = videoLikeService.isLiked(videoId, user.getId());
            result.put("success", true);
            result.put("liked", isLiked);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}