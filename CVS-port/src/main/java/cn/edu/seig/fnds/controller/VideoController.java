package cn.edu.seig.fnds.controller;

import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.entity.Video;
import cn.edu.seig.fnds.service.VideoService;
import cn.edu.seig.fnds.service.CityManagerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频控制器
 * 
 * @author lazzydu
 */
@RestController
@RequestMapping("/api/video")
@CrossOrigin
public class VideoController {

    private final VideoService videoService;
    private final CityManagerService cityManagerService;

    public VideoController(VideoService videoService, CityManagerService cityManagerService) {
        this.videoService = videoService;
        this.cityManagerService = cityManagerService;
    }

    /**
     * 分页查询视频列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listVideos(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "new") String sort) {

        Map<String, Object> result = new HashMap<>();
        try {
            IPage<Video> page = videoService.getVideoList(current, size, city, title, sort);
            result.put("success", true);
            result.put("data", page.getRecords());
            result.put("total", page.getTotal());
            result.put("current", page.getCurrent());
            result.put("pages", page.getPages());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 上传视频
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadVideo(@RequestBody Video video,
            @RequestAttribute("currentUser") User uploader) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证必填字段
            if (video.getTitle() == null || video.getTitle().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "视频标题不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            if (video.getVideoUrl() == null || video.getVideoUrl().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "视频链接不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            boolean success = videoService.uploadVideo(video, uploader);
            if (success) {
                result.put("success", true);
                result.put("message", "上传成功，等待审核");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "上传失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取视频详情
     */
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getVideo(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 将字符串ID转换为Long，避免JavaScript精度丢失问题
            Long videoId = Long.parseLong(id);
            Video video = videoService.getVideoDetail(videoId);
            // 只返回审核通过且未下架的视频
            boolean isApproved = video != null && video.getStatus() == 1;
            boolean isOnline = video != null && (video.getOfflineStatus() == null || video.getOfflineStatus() == 0);
            if (isApproved && isOnline) {
                result.put("success", true);
                result.put("data", video);
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "视频不存在、未审核通过或已下架");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的视频ID");
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取用户上传的视频
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserVideos(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Video> videos = videoService.getUserVideos(userId);
            result.put("success", true);
            result.put("data", videos);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取待审核视频列表（管理员功能）
     * 系统管理员可看到所有待审核视频
     * 城市管理员只能看到自己管理城市的待审核视频
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingVideos(@RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员（系统管理员或城市管理员）
        if (user.getRole() != 1 && user.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            List<Video> videos = videoService.getPendingVideosByUser(user);
            result.put("success", true);
            result.put("data", videos);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 管理员预览视频（可查看任意状态的视频）
     * 系统管理员可预览所有视频
     * 城市管理员只能预览自己管理城市的视频
     */
    @GetMapping("/preview/{id}")
    public ResponseEntity<Map<String, Object>> previewVideo(@PathVariable String id,
            @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员（系统管理员或城市管理员）
        if (user.getRole() != 1 && user.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            Long videoId = Long.parseLong(id);
            Video video = videoService.getVideoDetail(videoId);
            if (video != null) {
                // 城市管理员只能预览自己管理城市的视频
                if (user.getRole() == 2 && !cityManagerService.canManageVideo(user.getId(), video.getCity())) {
                    result.put("success", false);
                    result.put("message", "无权查看此视频");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
                }
                result.put("success", true);
                result.put("data", video);
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "视频不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的视频ID");
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 审核视频（管理员功能）
     * 系统管理员可审核所有视频
     * 城市管理员只能审核自己管理城市的视频
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<Map<String, Object>> reviewVideo(@PathVariable Long id,
            @RequestBody Map<String, Object> reviewRequest,
            @RequestAttribute("currentUser") User reviewer) {
        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员（系统管理员或城市管理员）
        if (reviewer.getRole() != 1 && reviewer.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            // 获取视频信息，检查城市管理员权限
            Video video = videoService.getById(id);
            if (video == null) {
                result.put("success", false);
                result.put("message", "视频不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }

            // 城市管理员只能审核自己管理城市的视频
            if (reviewer.getRole() == 2 && !cityManagerService.canManageVideo(reviewer.getId(), video.getCity())) {
                result.put("success", false);
                result.put("message", "无权审核此视频");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
            }

            Integer status = (Integer) reviewRequest.get("status");
            String reviewComment = (String) reviewRequest.get("reviewComment");

            if (status == null || (status != 1 && status != 2)) {
                result.put("success", false);
                result.put("message", "审核状态无效");
                return ResponseEntity.badRequest().body(result);
            }

            boolean success = videoService.reviewVideo(id, status, reviewComment, reviewer);
            if (success) {
                result.put("success", true);
                result.put("message", "审核完成");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "审核失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "审核失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 增加播放量
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Map<String, Object>> increaseViewCount(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            videoService.increaseViewCount(id);
            result.put("success", true);
            result.put("message", "播放量已更新");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 更新视频时长（从视频文件中获取真实时长）
     */
    @PostMapping("/{id}/update-duration")
    public ResponseEntity<Map<String, Object>> updateVideoDuration(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            int duration = videoService.updateVideoDuration(id);
            if (duration > 0) {
                result.put("success", true);
                result.put("message", "时长更新成功");
                result.put("duration", duration);
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "无法获取视频时长");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 批量更新所有视频的时长
     */
    @PostMapping("/batch-update-duration")
    public ResponseEntity<Map<String, Object>> batchUpdateDuration() {
        Map<String, Object> result = new HashMap<>();

        try {
            int count = videoService.batchUpdateAllVideoDuration();
            result.put("success", true);
            result.put("message", "批量更新完成");
            result.put("count", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 更新视频
     */
    @PutMapping(value = "/update", produces = "application/json")
    public ResponseEntity<Map<String, Object>> updateVideo(@RequestBody Video video,
            @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = videoService.updateVideo(video, user);
            if (success) {
                result.put("success", true);
                result.put("message", "更新成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "更新失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 删除视频
     */
    @DeleteMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> deleteVideo(@PathVariable Long id,
            @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = videoService.deleteVideo(id, user);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "删除失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 下架视频
     */
    @PostMapping(value = "/{id}/offline", produces = "application/json")
    public ResponseEntity<Map<String, Object>> offlineVideo(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody,
            @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            String reason = requestBody != null ? requestBody.get("reason") : null;
            boolean success = videoService.offlineVideo(id, user, reason);
            if (success) {
                result.put("success", true);
                result.put("message", "下架成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "下架失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "下架失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 重新上架视频
     */
    @PostMapping(value = "/{id}/online", produces = "application/json")
    public ResponseEntity<Map<String, Object>> onlineVideo(
            @PathVariable Long id,
            @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = videoService.onlineVideo(id, user);
            if (success) {
                result.put("success", true);
                result.put("message", "上架成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "上架失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "上架失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
