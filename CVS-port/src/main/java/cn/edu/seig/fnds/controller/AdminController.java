package cn.edu.seig.fnds.controller;

import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.entity.Video;
import cn.edu.seig.fnds.service.UserService;
import cn.edu.seig.fnds.service.VideoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.edu.seig.fnds.service.CityManagerService;

/**
 * 管理员控制器
 * 
 * @author lazzydu
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final VideoService videoService;
    private final UserService userService;
    private final CityManagerService cityManagerService;

    public AdminController(VideoService videoService, UserService userService, CityManagerService cityManagerService) {
        this.videoService = videoService;
        this.userService = userService;
        this.cityManagerService = cityManagerService;
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员（系统管理员或城市管理员）
        if (user.getRole() != 1 && user.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            Map<String, Object> stats = new HashMap<>();

            // 城市管理员只统计管理城市的数据
            if (user.getRole() == 2) {
                List<String> managedCities = cityManagerService.getManagedCityNames(user.getId());
                stats.put("managedCities", managedCities);

                if (managedCities.isEmpty()) {
                    stats.put("pendingVideos", 0);
                    stats.put("totalVideos", 0);
                    stats.put("todayUploads", 0);
                } else {
                    // 待审核视频数（管理城市）
                    long pendingVideos = videoService.count(new QueryWrapper<Video>()
                            .eq("status", 0)
                            .in("city", managedCities));
                    stats.put("pendingVideos", pendingVideos);

                    // 总视频数（管理城市）
                    long totalVideos = videoService.count(new QueryWrapper<Video>()
                            .in("city", managedCities));
                    stats.put("totalVideos", totalVideos);

                    // 今日上传数（管理城市）
                    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
                    long todayUploads = videoService.count(new QueryWrapper<Video>()
                            .ge("create_time", todayStart)
                            .in("city", managedCities));
                    stats.put("todayUploads", todayUploads);
                }
            } else {
                // 系统管理员统计所有数据
                long pendingVideos = videoService.count(new QueryWrapper<Video>().eq("status", 0));
                stats.put("pendingVideos", pendingVideos);

                long totalVideos = videoService.count();
                stats.put("totalVideos", totalVideos);

                long totalUsers = userService.count();
                stats.put("totalUsers", totalUsers);

                LocalDateTime todayStart = LocalDate.now().atStartOfDay();
                long todayUploads = videoService.count(new QueryWrapper<Video>()
                        .ge("create_time", todayStart));
                stats.put("todayUploads", todayUploads);
            }

            result.put("success", true);
            result.put("data", stats);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取所有视频列表（管理员）
     */
    @GetMapping("/videos")
    public ResponseEntity<Map<String, Object>> getAllVideos(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestAttribute("currentUser") User user) {

        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员（系统管理员或城市管理员）
        if (user.getRole() != 1 && user.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();

            // 城市管理员只能查看自己管理城市的视频
            if (user.getRole() == 2) {
                List<String> managedCities = cityManagerService.getManagedCityNames(user.getId());
                if (managedCities.isEmpty()) {
                    result.put("success", true);
                    result.put("data", List.of());
                    result.put("total", 0);
                    return ResponseEntity.ok(result);
                }
                queryWrapper.in("city", managedCities);
            }

            if (status != null) {
                queryWrapper.eq("status", status);
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.like("title", keyword);
            }
            queryWrapper.orderByDesc("create_time");

            IPage<Video> page = videoService.page(new Page<>(current, size), queryWrapper);

            // 填充上传者昵称
            for (Video video : page.getRecords()) {
                if (video.getUploaderId() != null) {
                    User uploader = userService.getById(video.getUploaderId());
                    if (uploader != null) {
                        video.setUploaderName(
                                uploader.getNickname() != null ? uploader.getNickname() : uploader.getUsername());
                    }
                }
            }

            result.put("success", true);
            result.put("data", page.getRecords());
            result.put("total", page.getTotal());
            result.put("current", page.getCurrent());
            result.put("pages", page.getPages());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取视频列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 删除视频（管理员）
     */
    @DeleteMapping("/video/{id}")
    public ResponseEntity<Map<String, Object>> deleteVideo(
            @PathVariable Long id,
            @RequestAttribute("currentUser") User user) {

        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员
        if (user.getRole() != 1) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            boolean success = videoService.removeById(id);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "视频不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
