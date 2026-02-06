package cn.edu.seig.fnds.service;

import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.entity.Video;
import cn.edu.seig.fnds.entity.Message;
import cn.edu.seig.fnds.mapper.VideoMapper;
import cn.edu.seig.fnds.util.VideoUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 视频服务类
 * 
 * @author lazzydu
 */
@Slf4j
@Service
public class VideoService extends ServiceImpl<VideoMapper, Video> {

    @org.springframework.beans.factory.annotation.Autowired
    private cn.edu.seig.fnds.mapper.UserMapper userMapper;

    @org.springframework.beans.factory.annotation.Autowired
    private MessageService messageService;

    @org.springframework.beans.factory.annotation.Autowired
    private CityManagerService cityManagerService;

    @org.springframework.beans.factory.annotation.Autowired
    private OSSService ossService;

    /**
     * 上传视频
     */
    public boolean uploadVideo(Video video, User uploader) {
        // 设置上传者信息
        video.setUploaderId(uploader.getId());
        video.setStatus(0); // 待审核状态
        video.setViewCount(0);
        video.setLikeCount(0);
        video.setCollectCount(0);
        video.setCreateTime(LocalDateTime.now());
        video.setUpdateTime(LocalDateTime.now());

        return save(video);
    }

    /**
     * 分页查询视频列表
     * 
     * @param sort 排序方式: "new" 按发布时间降序, "view" 按播放量降序
     */
    public IPage<Video> getVideoList(int current, int size, String city, String title, String sort) {
        Page<Video> page = new Page<>(current, size);

        var queryWrapper = Wrappers.<Video>lambdaQuery()
                .eq(Video::getStatus, 1) // 只查询审核通过的视频
                .and(wrapper -> wrapper
                        .isNull(Video::getOfflineStatus)
                        .or()
                        .eq(Video::getOfflineStatus, 0)) // 只查询未下架的视频
                .like(StringUtils.hasText(city), Video::getCity, city)
                .like(StringUtils.hasText(title), Video::getTitle, title);

        // 根据排序参数添加排序条件
        if ("view".equals(sort)) {
            // 按播放量降序，播放量相同按ID升序
            queryWrapper.orderByDesc(Video::getViewCount).orderByAsc(Video::getId);
        } else {
            // 默认按发布时间降序，发布时间相同按ID升序
            queryWrapper.orderByDesc(Video::getCreateTime).orderByAsc(Video::getId);
        }

        IPage<Video> result = page(page, queryWrapper);

        // 填充上传者昵称
        if (!result.getRecords().isEmpty()) {
            for (Video video : result.getRecords()) {
                User uploader = userMapper.selectById(video.getUploaderId());
                if (uploader != null) {
                    video.setUploaderName(uploader.getNickname());
                    video.setUploaderAvatar(uploader.getAvatar());
                } else {
                    video.setUploaderName("未知用户");
                }
            }
        }

        return result;
    }

    /**
     * 获取待审核视频列表（所有待审核视频）
     */
    public List<Video> getPendingVideos() {
        List<Video> videos = list(Wrappers.<Video>lambdaQuery()
                .eq(Video::getStatus, 0)
                .orderByDesc(Video::getCreateTime));

        // 填充上传者昵称
        fillUploaderInfo(videos);

        return videos;
    }

    /**
     * 获取待审核视频列表（按管理员角色过滤）
     * - 系统管理员(role=1)：返回所有待审核视频
     * - 城市管理员(role=2)：仅返回其管理城市的待审核视频
     */
    public List<Video> getPendingVideosByUser(User user) {
        List<Video> allPendingVideos = getPendingVideos();

        // 系统管理员可看到所有
        if (user.getRole() == 1) {
            return allPendingVideos;
        }

        // 城市管理员只能看到自己管理城市的视频
        if (user.getRole() == 2) {
            List<String> managedCities = cityManagerService.getManagedCityNames(user.getId());
            return allPendingVideos.stream()
                    .filter(video -> managedCities.contains(video.getCity()))
                    .toList();
        }

        // 其他角色无权限
        return List.of();
    }

    /**
     * 填充上传者信息
     */
    private void fillUploaderInfo(List<Video> videos) {
        if (!videos.isEmpty()) {
            for (Video video : videos) {
                User uploader = userMapper.selectById(video.getUploaderId());
                if (uploader != null) {
                    video.setUploaderName(uploader.getNickname());
                    video.setUploaderAvatar(uploader.getAvatar());
                } else {
                    video.setUploaderName("未知用户");
                }
            }
        }
    }

    /**
     * 审核视频
     */
    public boolean reviewVideo(Long videoId, Integer status, String reviewComment, User reviewer) {
        Video video = getById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }

        video.setStatus(status);
        video.setReviewerId(reviewer.getId());
        video.setReviewTime(LocalDateTime.now());
        video.setReviewComment(reviewComment);
        video.setUpdateTime(LocalDateTime.now());

        boolean success = updateById(video);

        // 发送系统通知
        if (success) {
            try {
                Message message = new Message();
                message.setUserId(video.getUploaderId());
                message.setFromUserId(reviewer.getId()); // 发送者为管理员
                message.setVideoId(video.getId());
                message.setType(4); // 4代表系统通知(需要确认MessageService支持的类型)
                message.setCreateTime(LocalDateTime.now());
                message.setIsRead(0);

                String statusText = status == 1 ? "通过" : "拒绝";
                String content = "您的视频《" + video.getTitle() + "》审核" + statusText;
                if (StringUtils.hasText(reviewComment)) {
                    content += "。备注: " + reviewComment;
                }
                message.setContent(content);

                messageService.save(message);
            } catch (Exception e) {
                log.error("发送审核通知失败", e);
            }
        }

        return success;
    }

    /**
     * 增加观看次数
     */
    public void incrementViewCount(Long videoId) {
        Video video = getById(videoId);
        if (video != null) {
            video.setViewCount(video.getViewCount() + 1);
            video.setUpdateTime(LocalDateTime.now());
            updateById(video);
        }
    }

    /**
     * 获取用户上传的视频
     */
    public List<Video> getUserVideos(Long userId) {
        List<Video> videos = list(Wrappers.<Video>lambdaQuery()
                .eq(Video::getUploaderId, userId)
                .orderByDesc(Video::getCreateTime));

        // 填充上传者信息
        if (!videos.isEmpty()) {
            User uploader = userMapper.selectById(userId);
            for (Video video : videos) {
                if (uploader != null) {
                    video.setUploaderName(uploader.getNickname());
                    video.setUploaderAvatar(uploader.getAvatar());
                } else {
                    video.setUploaderName("未知用户");
                }
            }
        }

        return videos;
    }

    /**
     * 更新单个视频的时长（从视频文件中获取）
     */
    public int updateVideoDuration(Long videoId) {
        Video video = getById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }

        String videoUrl = video.getVideoUrl();
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            throw new RuntimeException("视频URL为空");
        }

        // 将URL转换为本地文件路径
        String videoPath = convertUrlToPath(videoUrl);

        // 获取视频时长
        int duration = VideoUtil.getVideoDuration(videoPath);

        if (duration > 0) {
            video.setDuration(duration);
            video.setUpdateTime(LocalDateTime.now());
            updateById(video);
            System.out.println("视频ID " + videoId + " 时长已更新为: " + duration + "秒");
        }

        return duration;
    }

    /**
     * 批量更新所有视频的时长
     */
    public int batchUpdateAllVideoDuration() {
        List<Video> allVideos = list();
        int successCount = 0;

        System.out.println("开始批量更新视频时长，共 " + allVideos.size() + " 个视频");

        for (Video video : allVideos) {
            try {
                String videoUrl = video.getVideoUrl();
                if (videoUrl == null || videoUrl.trim().isEmpty()) {
                    System.out.println("跳过视频ID " + video.getId() + ": URL为空");
                    continue;
                }

                String videoPath = convertUrlToPath(videoUrl);
                int duration = VideoUtil.getVideoDuration(videoPath);

                if (duration > 0) {
                    video.setDuration(duration);
                    video.setUpdateTime(LocalDateTime.now());
                    updateById(video);
                    successCount++;
                    System.out.println("✓ 视频ID " + video.getId() + " (" + video.getTitle() + ") 时长: " + duration + "秒");
                } else {
                    System.out.println("✗ 视频ID " + video.getId() + " 无法获取时长");
                }

            } catch (Exception e) {
                System.err.println("✗ 更新视频ID " + video.getId() + " 失败: " + e.getMessage());
            }
        }

        System.out.println("批量更新完成！成功: " + successCount + "/" + allVideos.size());
        return successCount;
    }

    /**
     * 将视频URL转换为本地文件路径
     * 例如: http://localhost:8080/static/video/video1.mp4 ->
     * D:/path/to/CVS-port/src/main/resources/static/video/video1.mp4
     */
    private String convertUrlToPath(String videoUrl) {
        if (videoUrl.startsWith("http://") || videoUrl.startsWith("https://")) {
            // 提取路径部分
            // 例如: http://localhost:8080/static/video/video1.mp4 -> /static/video/video1.mp4
            String path = videoUrl.substring(videoUrl.indexOf("/static"));

            // 转换为本地文件系统路径
            String basePath = System.getProperty("user.dir");
            String localPath = basePath + File.separator + "src" + File.separator + "main" +
                    File.separator + "resources" + path.replace("/", File.separator);

            return localPath;
        }

        // 如果已经是本地路径，直接返回
        // 如果已经是本地路径，直接返回
        return videoUrl;
    }

    /**
     * 更新视频
     */
    public boolean updateVideo(Video video, User currentUser) {
        Video oldVideo = getById(video.getId());
        if (oldVideo == null) {
            throw new RuntimeException("视频不存在");
        }

        // 权限校验：仅作者或管理员可修改
        if (!oldVideo.getUploaderId().equals(currentUser.getId()) && currentUser.getRole() != 1) {
            throw new RuntimeException("无权修改此视频");
        }

        boolean needReview = false;

        // 如果修改了视频源或封面，需要重新审核（除非是管理员修改）
        if (currentUser.getRole() != 1) {
            if (!oldVideo.getVideoUrl().equals(video.getVideoUrl()) ||
                    (oldVideo.getThumbnailUrl() != null && !oldVideo.getThumbnailUrl().equals(video.getThumbnailUrl()))
                    ||
                    (video.getThumbnailUrl() != null && !video.getThumbnailUrl().equals(oldVideo.getThumbnailUrl()))) {
                needReview = true;
            }
        }

        // 更新字段
        oldVideo.setTitle(video.getTitle());
        oldVideo.setDescription(video.getDescription());
        oldVideo.setCity(video.getCity());
        oldVideo.setVideoUrl(video.getVideoUrl());
        oldVideo.setThumbnailUrl(video.getThumbnailUrl());
        oldVideo.setUpdateTime(LocalDateTime.now());

        if (needReview) {
            oldVideo.setStatus(0); // 重置为待审核
        }

        // 如果更新了视频源，尝试更新时长
        if (video.getVideoUrl() != null && !video.getVideoUrl().equals(oldVideo.getVideoUrl())) {
            try {
                String videoPath = convertUrlToPath(video.getVideoUrl());
                int duration = VideoUtil.getVideoDuration(videoPath);
                if (duration > 0) {
                    oldVideo.setDuration(duration);
                }
            } catch (Exception e) {
                log.error("更新视频时长失败", e);
            }
        }

        return updateById(oldVideo);
    }

    /**
     * 删除视频
     */
    public boolean deleteVideo(Long videoId, User currentUser) {
        Video video = getById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }

        // 权限校验：仅作者或管理员可删除
        if (!video.getUploaderId().equals(currentUser.getId()) && currentUser.getRole() != 1) {
            throw new RuntimeException("无权删除此视频");
        }

        // 删除关联数据（如评论、收藏等，视数据库外键策略而定，这里仅删除视频）
        // 实际项目中应级联删除或软删除，这里演示硬删除

        boolean success = removeById(videoId);

        // 如果删除成功，则删除OSS中的文件
        if (success) {
            System.out.println("视频ID " + videoId + " 数据库记录删除成功，准备删除OSS文件");
            // 异步或同步删除文件
            try {
                if (video.getVideoUrl() != null) {
                    System.out.println("准备删除视频文件: " + video.getVideoUrl());
                    ossService.deleteFile(video.getVideoUrl());
                }
                if (video.getThumbnailUrl() != null) {
                    System.out.println("准备删除封面文件: " + video.getThumbnailUrl());
                    ossService.deleteFile(video.getThumbnailUrl());
                }
            } catch (Exception e) {
                log.error("删除OSS文件失败", e);
                System.err.println("删除OSS文件流程发生异常: " + e.getMessage());
                // 不影响主业务
            }
        } else {
            System.err.println("视频ID " + videoId + " 数据库记录删除失败，跳过OSS文件删除");
        }

        return success;
    }

    /**
     * 下架视频
     */
    public boolean offlineVideo(Long videoId, User currentUser, String reason) {
        Video video = getById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }

        // 权限校验：发布者可以下架自己的视频，管理员可以下架所有视频
        boolean isOwner = video.getUploaderId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == 1;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("无权下架此视频");
        }

        // 管理员下架必须提供理由
        if (isAdmin && !isOwner && (reason == null || reason.trim().isEmpty())) {
            throw new RuntimeException("管理员下架视频必须提供理由");
        }

        // 设置下架状态
        video.setOfflineStatus(isAdmin && !isOwner ? 2 : 1); // 2=管理员下架，1=用户下架
        video.setOfflineReason(reason);
        video.setOfflineTime(LocalDateTime.now());
        video.setUpdateTime(LocalDateTime.now());

        boolean success = updateById(video);

        // 如果是管理员下架，发送消息通知
        if (success && isAdmin && !isOwner) {
            Message message = new Message();
            message.setFromUserId(currentUser.getId());
            message.setUserId(video.getUploaderId());
            message.setVideoId(videoId);
            message.setType(4); // 类型4：视频被下架通知
            message.setContent("您的视频《" + video.getTitle() + "》已被管理员下架。理由：" + reason);
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            messageService.save(message);
        }

        return success;
    }

    /**
     * 重新上架视频
     */
    public boolean onlineVideo(Long videoId, User currentUser) {
        Video video = getById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }

        // 权限校验：发布者可以上架自己的视频，管理员可以上架所有视频
        if (!video.getUploaderId().equals(currentUser.getId()) && currentUser.getRole() != 1) {
            throw new RuntimeException("无权上架此视频");
        }

        // 重置下架状态
        video.setOfflineStatus(0);
        video.setOfflineReason(null);
        video.setOfflineTime(null);
        video.setUpdateTime(LocalDateTime.now());

        return updateById(video);
    }

    public Video getVideoDetail(Long videoId) {
        Video video = getById(videoId);
        if (video != null) {
            User uploader = userMapper.selectById(video.getUploaderId());
            if (uploader != null) {
                video.setUploaderName(uploader.getNickname());
                video.setUploaderAvatar(uploader.getAvatar());
            } else {
                video.setUploaderName("未知用户");
            }
        }
        return video;
    }
}