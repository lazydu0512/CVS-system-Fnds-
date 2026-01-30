package cn.edu.seig.fnds.controller;

import cn.edu.seig.fnds.entity.City;
import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 城市管理控制器
 * 
 * @author lazzydu
 */
@RestController
@RequestMapping("/api/city")
@CrossOrigin
public class CityController {

    @Autowired
    private CityService cityService;

    /**
     * 获取所有城市列表（公开接口）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCities() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<City> cities = cityService.getAllCities();
            result.put("success", true);
            result.put("data", cities);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取城市列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 添加城市（管理员）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addCity(
            @RequestBody City city,
            @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        if (user.getRole() != 1) {
            result.put("success", false);
            result.put("message", "无权操作");
            return ResponseEntity.status(403).body(result);
        }

        if (city.getName() == null || city.getName().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "城市名称不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        if (cityService.checkNameExists(city.getName(), null)) {
            result.put("success", false);
            result.put("message", "城市名称已存在");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            city.setCreateTime(LocalDateTime.now());
            cityService.save(city);
            result.put("success", true);
            result.put("message", "添加成功");
            result.put("data", city);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 更新城市（管理员）
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCity(
            @PathVariable Long id,
            @RequestBody City city,
            @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        if (user.getRole() != 1) {
            result.put("success", false);
            result.put("message", "无权操作");
            return ResponseEntity.status(403).body(result);
        }

        if (city.getName() != null && cityService.checkNameExists(city.getName(), id)) {
            result.put("success", false);
            result.put("message", "城市名称已存在");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            city.setId(id);
            cityService.updateById(city);
            result.put("success", true);
            result.put("message", "更新成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 删除城市（管理员）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCity(
            @PathVariable Long id,
            @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        if (user.getRole() != 1) {
            result.put("success", false);
            result.put("message", "无权操作");
            return ResponseEntity.status(403).body(result);
        }

        try {
            cityService.removeById(id);
            result.put("success", true);
            result.put("message", "删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
