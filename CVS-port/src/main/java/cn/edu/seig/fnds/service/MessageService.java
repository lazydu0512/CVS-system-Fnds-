package cn.edu.seig.fnds.service;

import cn.edu.seig.fnds.entity.Message;
import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.entity.Video;
import cn.edu.seig.fnds.mapper.MessageMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知服务类
 * 
 * @author lazzydu
 */
@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {

    @Autowired
    private UserService userService;

    @Autowired
    @Lazy
    private VideoService videoService;

    // 消息类型常量
    public static final int TYPE_LIKE = 1; // 点赞
    public static final int TYPE_COMMENT = 2; // 评论
    public static final int TYPE_REPLY = 3; // 回复

    /**
     * 发送点赞通知
     */
    public void sendLikeMessage(Long fromUserId, Long toUserId, Long videoId) {
        // 不给自己发通知
        if (fromUserId.equals(toUserId)) {
            return;
        }

        Message message = new Message();
        message.setUserId(toUserId);
        message.setFromUserId(fromUserId);
        message.setType(TYPE_LIKE);
        message.setVideoId(videoId);
        message.setContent("赞了你的视频");
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        save(message);
    }

    /**
     * 发送评论通知
     */
    public void sendCommentMessage(Long fromUserId, Long toUserId, Long videoId, Long commentId, String content) {
        // 不给自己发通知
        if (fromUserId.equals(toUserId)) {
            return;
        }

        Message message = new Message();
        message.setUserId(toUserId);
        message.setFromUserId(fromUserId);
        message.setType(TYPE_COMMENT);
        message.setVideoId(videoId);
        message.setCommentId(commentId);
        message.setContent(content.length() > 50 ? content.substring(0, 50) + "..." : content);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        save(message);
    }

    /**
     * 发送回复通知
     */
    public void sendReplyMessage(Long fromUserId, Long toUserId, Long videoId, Long commentId, String content) {
        // 不给自己发通知
        if (fromUserId.equals(toUserId)) {
            return;
        }

        Message message = new Message();
        message.setUserId(toUserId);
        message.setFromUserId(fromUserId);
        message.setType(TYPE_REPLY);
        message.setVideoId(videoId);
        message.setCommentId(commentId);
        message.setContent(content.length() > 50 ? content.substring(0, 50) + "..." : content);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        save(message);
    }

    /**
     * 获取用户的消息列表
     */
    public List<Message> getUserMessages(Long userId) {
        List<Message> messages = list(Wrappers.<Message>lambdaQuery()
                .eq(Message::getUserId, userId)
                .orderByDesc(Message::getCreateTime));

        // 填充发送者和视频信息
        for (Message msg : messages) {
            User fromUser = userService.getById(msg.getFromUserId());
            if (fromUser != null) {
                User safeUser = new User();
                safeUser.setId(fromUser.getId());
                safeUser.setNickname(fromUser.getNickname());
                safeUser.setAvatar(fromUser.getAvatar());
                msg.setFromUser(safeUser);
            }

            if (msg.getVideoId() != null) {
                Video video = videoService.getById(msg.getVideoId());
                msg.setVideo(video);
            }
        }

        return messages;
    }

    /**
     * 获取未读消息数量
     */
    public long getUnreadCount(Long userId) {
        return count(Wrappers.<Message>lambdaQuery()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0));
    }

    /**
     * 标记消息为已读
     */
    public boolean markAsRead(Long messageId) {
        Message message = getById(messageId);
        if (message != null) {
            message.setIsRead(1);
            return updateById(message);
        }
        return false;
    }

    /**
     * 标记所有消息为已读
     */
    public boolean markAllAsRead(Long userId) {
        return update(Wrappers.<Message>lambdaUpdate()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1));
    }
}
