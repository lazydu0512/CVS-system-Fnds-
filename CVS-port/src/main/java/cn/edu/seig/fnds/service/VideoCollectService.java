package cn.edu.seig.fnds.service;

import cn.edu.seig.fnds.entity.Video;
import cn.edu.seig.fnds.entity.VideoCollect;
import cn.edu.seig.fnds.mapper.VideoCollectMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频收藏服务类
 * 
 * @author lazzydu
 */
@Service
public class VideoCollectService extends ServiceImpl<VideoCollectMapper, VideoCollect> {

    @Autowired
    private VideoService videoService;

    /**
     * 收藏/取消收藏
     */
    public boolean toggleCollect(Long videoId, Long userId) {
        VideoCollect existingCollect = getOne(Wrappers.<VideoCollect>lambdaQuery()
                .eq(VideoCollect::getVideoId, videoId)
                .eq(VideoCollect::getUserId, userId));

        Video video = videoService.getById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }

        if (existingCollect != null) {
            // 取消收藏
            removeById(existingCollect.getId());
            video.setCollectCount(video.getCollectCount() - 1);
        } else {
            // 收藏
            VideoCollect collect = new VideoCollect();
            collect.setVideoId(videoId);
            collect.setUserId(userId);
            collect.setCreateTime(LocalDateTime.now());
            save(collect);
            video.setCollectCount(video.getCollectCount() + 1);
        }

        video.setUpdateTime(LocalDateTime.now());
        videoService.updateById(video);
        return true;
    }

    /**
     * 检查用户是否已收藏
     */
    public boolean isCollected(Long videoId, Long userId) {
        return count(Wrappers.<VideoCollect>lambdaQuery()
                .eq(VideoCollect::getVideoId, videoId)
                .eq(VideoCollect::getUserId, userId)) > 0;
    }

    /**
     * 获取用户收藏列表（包含视频详细信息）
     */
    public List<VideoCollect> getUserCollects(Long userId) {
        List<VideoCollect> collects = list(Wrappers.<VideoCollect>lambdaQuery()
                .eq(VideoCollect::getUserId, userId)
                .orderByDesc(VideoCollect::getCreateTime));

        // 为每条收藏记录填充视频信息
        for (VideoCollect collect : collects) {
            Video video = videoService.getById(collect.getVideoId());
            collect.setVideo(video);
        }

        return collects;
    }
}