package cn.edu.seig.fnds.controller;

import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard控制器
 * 
 * @author lazzydu
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取总览数据
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview(@RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        // 检查权限
        if (user.getRole() != 1 && user.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            Map<String, Object> overview = dashboardService.getOverview(user.getId(), user.getRole());
            result.put("success", true);
            result.put("data", overview);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取视频浏览量趋势
     */
    @GetMapping("/video-views-trend")
    public ResponseEntity<Map<String, Object>> getVideoViewsTrend(
            @RequestParam(defaultValue = "30") Integer days,
            @RequestAttribute("currentUser") User user) {

        Map<String, Object> result = new HashMap<>();

        if (user.getRole() != 1 && user.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            List<Map<String, Object>> trend = dashboardService.getVideoViewsTrend(days, user.getId(), user.getRole());
            result.put("success", true);
            result.put("data", trend);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取用户注册量趋势
     */
    @GetMapping("/user-register-trend")
    public ResponseEntity<Map<String, Object>> getUserRegisterTrend(
            @RequestParam(defaultValue = "30") Integer days,
            @RequestAttribute("currentUser") User user) {

        Map<String, Object> result = new HashMap<>();

        // 仅系统管理员可查看
        if (user.getRole() != 1) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            List<Map<String, Object>> trend = dashboardService.getUserRegisterTrend(days);
            result.put("success", true);
            result.put("data", trend);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取视频上传量趋势
     */
    @GetMapping("/video-upload-trend")
    public ResponseEntity<Map<String, Object>> getVideoUploadTrend(
            @RequestParam(defaultValue = "30") Integer days,
            @RequestAttribute("currentUser") User user) {

        Map<String, Object> result = new HashMap<>();

        if (user.getRole() != 1 && user.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            List<Map<String, Object>> trend = dashboardService.getVideoUploadTrend(days, user.getId(), user.getRole());
            result.put("success", true);
            result.put("data", trend);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取互动数据统计
     */
    @GetMapping("/interaction-stats")
    public ResponseEntity<Map<String, Object>> getInteractionStats(@RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        if (user.getRole() != 1 && user.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            Map<String, Object> stats = dashboardService.getInteractionStats(user.getId(), user.getRole());
            result.put("success", true);
            result.put("data", stats);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取城市视频分布
     */
    @GetMapping("/city-distribution")
    public ResponseEntity<Map<String, Object>> getCityDistribution(@RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        if (user.getRole() != 1 && user.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            List<Map<String, Object>> distribution = dashboardService.getCityDistribution(user.getId(), user.getRole());
            result.put("success", true);
            result.put("data", distribution);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取热门视频排行
     */
    @GetMapping("/top-videos")
    public ResponseEntity<Map<String, Object>> getTopVideos(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestAttribute("currentUser") User user) {

        Map<String, Object> result = new HashMap<>();

        if (user.getRole() != 1 && user.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            List<Map<String, Object>> topVideos = dashboardService.getTopVideos(limit, user.getId(), user.getRole());
            result.put("success", true);
            result.put("data", topVideos);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
