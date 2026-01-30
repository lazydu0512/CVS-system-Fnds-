package cn.edu.seig.fnds.controller;

import cn.edu.seig.fnds.entity.Message;
import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知控制器
 * 
 * @author lazzydu
 */
@RestController
@RequestMapping("/api/message")
@CrossOrigin
public class MessageController {

    private final MessageService messageService;
    private final cn.edu.seig.fnds.service.VideoService videoService;

    public MessageController(MessageService messageService, cn.edu.seig.fnds.service.VideoService videoService) {
        this.messageService = messageService;
        this.videoService = videoService;
    }

    /**
     * 获取用户消息列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getMessages(@RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Message> messages = messageService.getUserMessages(user.getId());
            result.put("success", true);
            result.put("data", messages);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取消息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            long count = messageService.getUnreadCount(user.getId());

            // 如果是管理员，加上待审核视频数量
            if (user.getRole() == 1) {
                long pendingCount = videoService.count(
                        com.baomidou.mybatisplus.core.toolkit.Wrappers.<cn.edu.seig.fnds.entity.Video>lambdaQuery()
                                .eq(cn.edu.seig.fnds.entity.Video::getStatus, 0));
                count += pendingCount;
            }

            result.put("success", true);
            result.put("count", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取未读数量失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 标记单条消息为已读
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = messageService.markAsRead(id);
            if (success) {
                result.put("success", true);
                result.put("message", "标记成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "消息不存在");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 标记所有消息为已读
     */
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            messageService.markAllAsRead(user.getId());
            result.put("success", true);
            result.put("message", "全部标记为已读");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
