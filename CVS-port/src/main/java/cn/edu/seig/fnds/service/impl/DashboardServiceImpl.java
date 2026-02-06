package cn.edu.seig.fnds.service.impl;

import cn.edu.seig.fnds.entity.Comment;
import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.entity.Video;
import cn.edu.seig.fnds.service.CityManagerService;
import cn.edu.seig.fnds.service.CommentService;
import cn.edu.seig.fnds.service.DashboardService;
import cn.edu.seig.fnds.service.UserService;
import cn.edu.seig.fnds.service.VideoService;
import cn.edu.seig.fnds.service.ViewLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard服务实现类
 * 
 * @author lazzydu
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    @Autowired
    private CityManagerService cityManagerService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ViewLogService viewLogService;

    public DashboardServiceImpl(VideoService videoService, UserService userService,
            CityManagerService cityManagerService, CommentService commentService) {
        this.videoService = videoService;
        this.userService = userService;
        this.cityManagerService = cityManagerService;
        this.commentService = commentService;
    }

    @Override
    public Map<String, Object> getOverview(Long userId, Integer role) {
        Map<String, Object> overview = new HashMap<>();

        if (role == 2) {
            // 城市管理员
            List<String> managedCities = cityManagerService.getManagedCityNames(userId);
            if (managedCities.isEmpty()) {
                overview.put("totalVideos", 0);
                overview.put("totalViews", 0);
                overview.put("totalLikes", 0);
                overview.put("totalComments", 0);
                return overview;
            }

            QueryWrapper<Video> wrapper = new QueryWrapper<>();
            wrapper.in("city", managedCities).eq("status", 1);
            List<Video> videos = videoService.list(wrapper);

            overview.put("totalVideos", videos.size());
            overview.put("totalViews", videos.stream().mapToInt(Video::getViewCount).sum());
            overview.put("totalLikes", videos.stream().mapToInt(Video::getLikeCount).sum());
            overview.put("totalCollects", videos.stream().mapToInt(Video::getCollectCount).sum());
        } else {
            // 系统管理员
            long totalVideos = videoService.count(new QueryWrapper<Video>().eq("status", 1));
            long totalUsers = userService.count();

            List<Video> allVideos = videoService.list(new QueryWrapper<Video>().eq("status", 1));
            int totalViews = allVideos.stream().mapToInt(Video::getViewCount).sum();
            int totalLikes = allVideos.stream().mapToInt(Video::getLikeCount).sum();
            int totalCollects = allVideos.stream().mapToInt(Video::getCollectCount).sum();

            overview.put("totalVideos", totalVideos);
            overview.put("totalUsers", totalUsers);
            overview.put("totalViews", totalViews);
            overview.put("totalLikes", totalLikes);
            overview.put("totalCollects", totalCollects);
        }

        return overview;
    }

    @Override
    public List<Map<String, Object>> getVideoViewsTrend(Integer days, Long userId, Integer role) {
        LocalDateTime startDate = LocalDate.now().minusDays(days - 1).atStartOfDay();

        // 获取需要统计的视频ID列表
        List<Long> videoIds = null;
        if (role == 2) {
            // 城市管理员：只统计管理城市的视频
            List<String> managedCities = cityManagerService.getManagedCityNames(userId);
            if (managedCities.isEmpty()) {
                return new ArrayList<>();
            }
            QueryWrapper<Video> wrapper = new QueryWrapper<>();
            wrapper.in("city", managedCities).eq("status", 1);
            videoIds = videoService.list(wrapper).stream()
                    .map(Video::getId)
                    .collect(Collectors.toList());
        }

        // 从浏览日志获取每日统计
        List<Map<String, Object>> dailyStats = viewLogService.getDailyViewStats(startDate, videoIds);

        // 补全缺失的日期（浏览量为0的日期）
        Map<LocalDate, Integer> dailyViews = new TreeMap<>();
        for (int i = 0; i < days; i++) {
            dailyViews.put(LocalDate.now().minusDays(days - 1 - i), 0);
        }

        for (Map<String, Object> stat : dailyStats) {
            LocalDate date = LocalDate.parse(stat.get("date").toString());
            Integer count = ((Number) stat.get("count")).intValue();
            dailyViews.put(date, count);
        }

        // 转换为结果格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : dailyViews.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey().toString());
            item.put("views", entry.getValue());
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getUserRegisterTrend(Integer days) {
        LocalDateTime startDate = LocalDate.now().minusDays(days - 1).atStartOfDay();

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startDate);
        List<User> users = userService.list(wrapper);

        // 按日期分组统计
        Map<LocalDate, Long> dailyRegisters = new TreeMap<>();
        for (int i = 0; i < days; i++) {
            dailyRegisters.put(LocalDate.now().minusDays(days - 1 - i), 0L);
        }

        for (User user : users) {
            LocalDate date = user.getCreateTime().toLocalDate();
            dailyRegisters.merge(date, 1L, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> entry : dailyRegisters.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey().toString());
            item.put("count", entry.getValue());
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getVideoUploadTrend(Integer days, Long userId, Integer role) {
        LocalDateTime startDate = LocalDate.now().minusDays(days - 1).atStartOfDay();

        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startDate);

        // 城市管理员权限过滤
        if (role == 2) {
            List<String> managedCities = cityManagerService.getManagedCityNames(userId);
            if (managedCities.isEmpty()) {
                return new ArrayList<>();
            }
            wrapper.in("city", managedCities);
        }

        List<Video> videos = videoService.list(wrapper);

        // 按日期分组统计
        Map<LocalDate, Long> dailyUploads = new TreeMap<>();
        for (int i = 0; i < days; i++) {
            dailyUploads.put(LocalDate.now().minusDays(days - 1 - i), 0L);
        }

        for (Video video : videos) {
            LocalDate date = video.getCreateTime().toLocalDate();
            dailyUploads.merge(date, 1L, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> entry : dailyUploads.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey().toString());
            item.put("count", entry.getValue());
            result.add(item);
        }

        return result;
    }

    @Override
    public Map<String, Object> getInteractionStats(Long userId, Integer role) {
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);

        int totalComments;

        // 城市管理员权限过滤
        if (role == 2) {
            List<String> managedCities = cityManagerService.getManagedCityNames(userId);
            if (managedCities.isEmpty()) {
                Map<String, Object> emptyStats = new HashMap<>();
                emptyStats.put("likes", 0);
                emptyStats.put("collects", 0);
                emptyStats.put("comments", 0);
                return emptyStats;
            }
            wrapper.in("city", managedCities);

            // 获取城市管理员管理的城市的视频列表
            List<Video> videos = videoService.list(wrapper);
            int totalLikes = videos.stream().mapToInt(Video::getLikeCount).sum();
            int totalCollects = videos.stream().mapToInt(Video::getCollectCount).sum();

            // 统计这些视频的评论数
            if (videos.isEmpty()) {
                totalComments = 0;
            } else {
                List<Long> videoIds = videos.stream().map(Video::getId).collect(Collectors.toList());
                QueryWrapper<Comment> commentWrapper = new QueryWrapper<>();
                commentWrapper.in("video_id", videoIds).eq("status", 0);
                totalComments = (int) commentService.count(commentWrapper);
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("likes", totalLikes);
            stats.put("collects", totalCollects);
            stats.put("comments", totalComments);
            return stats;
        }

        // 系统管理员统计所有数据
        List<Video> videos = videoService.list(wrapper);
        int totalLikes = videos.stream().mapToInt(Video::getLikeCount).sum();
        int totalCollects = videos.stream().mapToInt(Video::getCollectCount).sum();

        // 统计所有正常状态的评论
        QueryWrapper<Comment> commentWrapper = new QueryWrapper<>();
        commentWrapper.eq("status", 0);
        totalComments = (int) commentService.count(commentWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("likes", totalLikes);
        stats.put("collects", totalCollects);
        stats.put("comments", totalComments);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getCityDistribution(Long userId, Integer role) {
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);

        // 城市管理员权限过滤
        if (role == 2) {
            List<String> managedCities = cityManagerService.getManagedCityNames(userId);
            if (managedCities.isEmpty()) {
                return new ArrayList<>();
            }
            wrapper.in("city", managedCities);
        }

        List<Video> videos = videoService.list(wrapper);

        // 按城市分组统计
        Map<String, Long> cityCount = videos.stream()
                .collect(Collectors.groupingBy(Video::getCity, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : cityCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            result.add(item);
        }

        // 按数量降序排序
        result.sort((a, b) -> ((Long) b.get("value")).compareTo((Long) a.get("value")));

        return result;
    }

    @Override
    public List<Map<String, Object>> getTopVideos(Integer limit, Long userId, Integer role) {
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);

        // 城市管理员权限过滤
        if (role == 2) {
            List<String> managedCities = cityManagerService.getManagedCityNames(userId);
            if (managedCities.isEmpty()) {
                return new ArrayList<>();
            }
            wrapper.in("city", managedCities);
        }

        wrapper.orderByDesc("view_count").last("LIMIT " + limit);
        List<Video> videos = videoService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Video video : videos) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", video.getId());
            item.put("title", video.getTitle());
            item.put("viewCount", video.getViewCount());
            item.put("likeCount", video.getLikeCount());
            item.put("city", video.getCity());
            item.put("thumbnailUrl", video.getThumbnailUrl());

            // 获取上传者信息
            if (video.getUploaderId() != null) {
                User uploader = userService.getById(video.getUploaderId());
                if (uploader != null) {
                    item.put("uploaderName",
                            uploader.getNickname() != null ? uploader.getNickname() : uploader.getUsername());
                }
            }

            result.add(item);
        }

        return result;
    }
}
