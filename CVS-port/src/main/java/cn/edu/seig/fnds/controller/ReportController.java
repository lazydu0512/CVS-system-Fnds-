package cn.edu.seig.fnds.controller;

import cn.edu.seig.fnds.entity.Report;
import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 举报控制器
 * 
 * @author lazzydu
 */
@RestController
@RequestMapping("/api/report")
@CrossOrigin
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 提交举报
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitReport(
            @RequestBody Report report,
            @RequestAttribute("currentUser") User currentUser) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证必填字段
            if (report.getTargetId() == null) {
                result.put("success", false);
                result.put("message", "被举报目标ID不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            if (report.getTargetType() == null) {
                result.put("success", false);
                result.put("message", "举报目标类型不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            if (report.getReasonCategory() == null || report.getReasonCategory().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "举报分类不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            boolean success = reportService.submitReport(report, currentUser.getId());
            if (success) {
                result.put("success", true);
                result.put("message", "举报提交成功，管理员将尽快处理");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "举报提交失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "举报失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取举报列表（管理员）
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getReportList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String city,
            @RequestAttribute("currentUser") User currentUser) {
        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员
        if (currentUser.getRole() != 1 && currentUser.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            List<Report> reports = reportService.getReportList(currentUser, status, city);
            result.put("success", true);
            result.put("data", reports);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 审核通过举报
     */
    @PostMapping("/approve/{id}")
    public ResponseEntity<Map<String, Object>> approveReport(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody,
            @RequestAttribute("currentUser") User currentUser) {
        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员
        if (currentUser.getRole() != 1 && currentUser.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            String reviewComment = requestBody != null ? requestBody.get("reviewComment") : null;
            boolean success = reportService.approveReport(id, currentUser.getId(), reviewComment);
            if (success) {
                result.put("success", true);
                result.put("message", "审核通过，已处理违规内容");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "审核失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "审核失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 驳回举报
     */
    @PostMapping("/reject/{id}")
    public ResponseEntity<Map<String, Object>> rejectReport(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody,
            @RequestAttribute("currentUser") User currentUser) {
        Map<String, Object> result = new HashMap<>();

        // 检查是否为管理员
        if (currentUser.getRole() != 1 && currentUser.getRole() != 2) {
            result.put("success", false);
            result.put("message", "权限不足");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        try {
            String reviewComment = requestBody != null ? requestBody.get("reviewComment") : null;
            boolean success = reportService.rejectReport(id, currentUser.getId(), reviewComment);
            if (success) {
                result.put("success", true);
                result.put("message", "已驳回举报");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "驳回失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
