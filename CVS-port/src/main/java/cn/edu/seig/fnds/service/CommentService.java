package cn.edu.seig.fnds.service;

import cn.edu.seig.fnds.entity.Comment;
import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.entity.Video;
import cn.edu.seig.fnds.mapper.CommentMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论服务类
 * 
 * @author lazzydu
 */
@Service
public class CommentService extends ServiceImpl<CommentMapper, Comment> {

    @Autowired
    private UserService userService;

    @Autowired
    @Lazy
    private VideoService videoService;

    @Autowired
    @Lazy
    private MessageService messageService;

    /**
     * 添加评论
     */
    public boolean addComment(Comment comment) {
        comment.setStatus(0); // 正常状态
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        boolean saved = save(comment);

        if (saved) {
            // 异步发送消息通知，不影响评论保存的结果
            try {
                Video video = videoService.getById(comment.getVideoId());

                if (comment.getParentId() != null) {
                    // 这是回复，确定通知对象
                    Long notifyUserId;

                    if (comment.getReplyToUserId() != null) {
                        // 优先使用指定的被回复用户ID（讨论场景）
                        notifyUserId = comment.getReplyToUserId();
                    } else {
                        // 默认通知父评论的作者
                        Comment parentComment = getById(comment.getParentId());
                        notifyUserId = parentComment != null ? parentComment.getUserId() : null;
                    }

                    if (notifyUserId != null) {
                        messageService.sendReplyMessage(
                                comment.getUserId(),
                                notifyUserId,
                                comment.getVideoId(),
                                comment.getId(),
                                comment.getContent());
                    }
                } else if (video != null) {
                    // 这是评论，通知视频作者
                    messageService.sendCommentMessage(
                            comment.getUserId(),
                            video.getUploaderId(),
                            comment.getVideoId(),
                            comment.getId(),
                            comment.getContent());
                }
            } catch (Exception e) {
                // 消息发送失败不应该影响评论保存的结果，只记录日志
                System.err.println("发送消息通知失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return saved;
    }

    /**
     * 获取视频评论列表（包含用户信息和回复）
     */
    public List<Comment> getVideoComments(Long videoId) {
        List<Comment> comments = list(Wrappers.<Comment>lambdaQuery()
                .eq(Comment::getVideoId, videoId)
                .eq(Comment::getStatus, 0) // 只查询正常状态的评论
                .isNull(Comment::getParentId) // 只查询顶级评论
                .orderByDesc(Comment::getCreateTime));

        // 为每条评论填充用户信息和回复
        for (Comment comment : comments) {
            fillCommentUserInfo(comment);
            // 获取回复列表
            List<Comment> replies = getCommentReplies(comment.getId());
            for (Comment reply : replies) {
                fillCommentUserInfo(reply);
            }
            comment.setReplies(replies);
        }

        return comments;
    }

    /**
     * 填充评论的用户信息
     */
    private void fillCommentUserInfo(Comment comment) {
        User user = userService.getById(comment.getUserId());
        if (user != null) {
            // 只保留必要的用户信息，隐藏敏感数据
            User safeUser = new User();
            safeUser.setId(user.getId());
            safeUser.setUsername(user.getUsername());
            safeUser.setNickname(user.getNickname());
            safeUser.setAvatar(user.getAvatar());
            comment.setUser(safeUser);
        }
    }

    /**
     * 获取评论回复
     */
    public List<Comment> getCommentReplies(Long parentId) {
        return list(Wrappers.<Comment>lambdaQuery()
                .eq(Comment::getParentId, parentId)
                .eq(Comment::getStatus, 0)
                .orderByAsc(Comment::getCreateTime));
    }

    /**
     * 删除评论
     */
    public boolean deleteComment(Long commentId) {
        Comment comment = getById(commentId);
        if (comment != null) {
            comment.setStatus(1); // 隐藏状态
            comment.setUpdateTime(LocalDateTime.now());
            return updateById(comment);
        }
        return false;
    }

    /**
     * 获取用户的所有评论（包含视频信息）
     */
    public List<Comment> getUserComments(Long userId) {
        List<Comment> comments = list(Wrappers.<Comment>lambdaQuery()
                .eq(Comment::getUserId, userId)
                .eq(Comment::getStatus, 0)
                .orderByDesc(Comment::getCreateTime));

        // 为每条评论填充视频信息
        for (Comment comment : comments) {
            Video video = videoService.getById(comment.getVideoId());
            comment.setVideo(video);
        }

        return comments;
    }
}