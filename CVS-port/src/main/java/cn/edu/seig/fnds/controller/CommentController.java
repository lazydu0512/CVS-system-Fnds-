package cn.edu.seig.fnds.controller;

import cn.edu.seig.fnds.entity.Comment;
import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论控制器
 * 
 * @author lazzydu
 */
@RestController
@RequestMapping("/api/comment")
@CrossOrigin
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 添加评论
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addComment(@RequestBody Comment comment,
            @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "评论内容不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            comment.setUserId(user.getId());
            boolean success = commentService.addComment(comment);
            if (success) {
                result.put("success", true);
                result.put("message", "评论成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "评论失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "评论失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取视频评论列表
     */
    @GetMapping("/video/{videoId}")
    public ResponseEntity<Map<String, Object>> getVideoComments(@PathVariable Long videoId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Comment> comments = commentService.getVideoComments(videoId);
            result.put("success", true);
            result.put("data", comments);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取评论回复
     */
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Map<String, Object>> getCommentReplies(@PathVariable Long commentId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Comment> replies = commentService.getCommentReplies(commentId);
            result.put("success", true);
            result.put("data", replies);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Long id,
            @RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            Comment comment = commentService.getById(id);
            if (comment == null) {
                result.put("success", false);
                result.put("message", "评论不存在");
                return ResponseEntity.notFound().build();
            }

            // 检查权限：只能删除自己的评论或管理员删除
            if (!comment.getUserId().equals(user.getId()) && user.getRole() != 1) {
                result.put("success", false);
                result.put("message", "权限不足");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
            }

            boolean success = commentService.deleteComment(id);
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
     * 获取当前用户的所有评论
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserComments(@RequestAttribute("currentUser") User user) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Comment> comments = commentService.getUserComments(user.getId());
            result.put("success", true);
            result.put("data", comments);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}