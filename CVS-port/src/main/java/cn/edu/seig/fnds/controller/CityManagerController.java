package cn.edu.seig.fnds.controller;

import cn.edu.seig.fnds.entity.CityManager;
import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.service.CityManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 城市管理员控制器
 * 系统管理员专用接口
 * 
 * @author lazzydu
 */
@RestController
@RequestMapping("/api/city-manager")
public class CityManagerController {

    private final CityManagerService cityManagerService;

    public CityManagerController(CityManagerService cityManagerService) {
        this.cityManagerService = cityManagerService;
    }

    /**
     * 获取所有城市管理员及其管理的城市
     * 仅系统管理员可访问
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllCityManagers(
            @RequestAttribute("currentUser") User admin) {
        Map<String, Object> result = new HashMap<>();

        if (admin.getRole() != 1) {
            result.put("success", false);
            result.put("message", "无权限访问");
            return ResponseEntity.status(403).body(result);
        }

        List<CityManager> cityManagers = cityManagerService.getAllCityManagersWithDetails();
        List<User> managerUsers = cityManagerService.getAllCityManagerUsers();

        result.put("success", true);
        result.put("cityManagers", cityManagers);
        result.put("managerUsers", managerUsers);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取指定管理员管理的城市列表
     */
    @GetMapping("/{userId}/cities")
    public ResponseEntity<Map<String, Object>> getManagedCities(
            @PathVariable Long userId,
            @RequestAttribute("currentUser") User admin) {
        Map<String, Object> result = new HashMap<>();

        // 系统管理员或自己可以查看
        if (admin.getRole() != 1 && !admin.getId().equals(userId)) {
            result.put("success", false);
            result.put("message", "无权限访问");
            return ResponseEntity.status(403).body(result);
        }

        List<CityManager> managedCities = cityManagerService.getManagedCities(userId);
        result.put("success", true);
        result.put("data", managedCities);
        return ResponseEntity.ok(result);
    }

    /**
     * 分配城市给管理员
     * 仅系统管理员可操作
     */
    @PostMapping("/assign")
    public ResponseEntity<Map<String, Object>> assignCity(
            @RequestBody Map<String, Long> request,
            @RequestAttribute("currentUser") User admin) {
        Map<String, Object> result = new HashMap<>();

        if (admin.getRole() != 1) {
            result.put("success", false);
            result.put("message", "无权限操作");
            return ResponseEntity.status(403).body(result);
        }

        Long userId = request.get("userId");
        Long cityId = request.get("cityId");

        if (userId == null || cityId == null) {
            result.put("success", false);
            result.put("message", "参数错误");
            return ResponseEntity.badRequest().body(result);
        }

        String error = cityManagerService.assignCity(userId, cityId);
        if (error != null) {
            result.put("success", false);
            result.put("message", error);
            return ResponseEntity.badRequest().body(result);
        }

        result.put("success", true);
        result.put("message", "城市分配成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 移除管理员的城市权限
     * 仅系统管理员可操作
     */
    @DeleteMapping("/{userId}/city/{cityId}")
    public ResponseEntity<Map<String, Object>> removeCity(
            @PathVariable Long userId,
            @PathVariable Long cityId,
            @RequestAttribute("currentUser") User admin) {
        Map<String, Object> result = new HashMap<>();

        if (admin.getRole() != 1) {
            result.put("success", false);
            result.put("message", "无权限操作");
            return ResponseEntity.status(403).body(result);
        }

        String error = cityManagerService.removeCity(userId, cityId);
        if (error != null) {
            result.put("success", false);
            result.put("message", error);
            return ResponseEntity.badRequest().body(result);
        }

        result.put("success", true);
        result.put("message", "城市权限已移除");
        return ResponseEntity.ok(result);
    }

    /**
     * 设置用户为城市管理员
     * 仅系统管理员可操作
     */
    @PostMapping("/set-role/{userId}")
    public ResponseEntity<Map<String, Object>> setUserAsCityManager(
            @PathVariable Long userId,
            @RequestAttribute("currentUser") User admin) {
        Map<String, Object> result = new HashMap<>();

        if (admin.getRole() != 1) {
            result.put("success", false);
            result.put("message", "无权限操作");
            return ResponseEntity.status(403).body(result);
        }

        String error = cityManagerService.setUserAsCityManager(userId);
        if (error != null) {
            result.put("success", false);
            result.put("message", error);
            return ResponseEntity.badRequest().body(result);
        }

        result.put("success", true);
        result.put("message", "已设置为城市管理员");
        return ResponseEntity.ok(result);
    }

    /**
     * 取消用户的城市管理员角色
     * 仅系统管理员可操作
     */
    @DeleteMapping("/remove-role/{userId}")
    public ResponseEntity<Map<String, Object>> removeCityManagerRole(
            @PathVariable Long userId,
            @RequestAttribute("currentUser") User admin) {
        Map<String, Object> result = new HashMap<>();

        if (admin.getRole() != 1) {
            result.put("success", false);
            result.put("message", "无权限操作");
            return ResponseEntity.status(403).body(result);
        }

        String error = cityManagerService.removeCityManagerRole(userId);
        if (error != null) {
            result.put("success", false);
            result.put("message", error);
            return ResponseEntity.badRequest().body(result);
        }

        result.put("success", true);
        result.put("message", "已取消城市管理员角色");
        return ResponseEntity.ok(result);
    }
}
