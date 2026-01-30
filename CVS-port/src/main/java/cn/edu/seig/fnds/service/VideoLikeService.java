package cn.edu.seig.fnds.service;

import cn.edu.seig.fnds.entity.Video;
import cn.edu.seig.fnds.entity.VideoLike;
import cn.edu.seig.fnds.mapper.VideoLikeMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 视频点赞服务类
 * 
 * @author lazzydu
 */
@Service
public class VideoLikeService extends ServiceImpl<VideoLikeMapper, VideoLike> {

    @Autowired
    private VideoService videoService;

    @Autowired
    @Lazy
    private MessageService messageService;

    /**
     * 点赞/取消点赞
     */
    public boolean toggleLike(Long videoId, Long userId) {
        VideoLike existingLike = getOne(Wrappers.<VideoLike>lambdaQuery()
                .eq(VideoLike::getVideoId, videoId)
                .eq(VideoLike::getUserId, userId));

        Video video = videoService.getById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }

        if (existingLike != null) {
            // 取消点赞
            removeById(existingLike.getId());
            video.setLikeCount(video.getLikeCount() - 1);
        } else {
            // 点赞
            VideoLike like = new VideoLike();
            like.setVideoId(videoId);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            save(like);
            video.setLikeCount(video.getLikeCount() + 1);

            // 发送点赞通知给视频作者
            messageService.sendLikeMessage(userId, video.getUploaderId(), videoId);
        }

        video.setUpdateTime(LocalDateTime.now());
        videoService.updateById(video);
        return true;
    }

    /**
     * 检查用户是否已点赞
     */
    public boolean isLiked(Long videoId, Long userId) {
        return count(Wrappers.<VideoLike>lambdaQuery()
                .eq(VideoLike::getVideoId, videoId)
                .eq(VideoLike::getUserId, userId)) > 0;
    }
}