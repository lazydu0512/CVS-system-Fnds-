package cn.edu.seig.fnds.service.impl;

import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.entity.Video;
import cn.edu.seig.fnds.service.CityManagerService;
import cn.edu.seig.fnds.service.DashboardService;
import cn.edu.seig.fnds.service.UserService;
import cn.edu.seig.fnds.service.VideoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
@Service
public class DashboardServiceImpl implements DashboardService {

    private final VideoService videoService;
    private final UserService userService;
    private final CityManagerService cityManagerService;

    public DashboardServiceImpl(VideoService videoService, UserService userService,
            CityManagerService cityManagerService) {
        this.videoService = videoService;
        this.userService = userService;
        this.cityManagerService = cityManagerService;
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

        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startDate).eq("status", 1);

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
        Map<LocalDate, Integer> dailyViews = new TreeMap<>();
        for (int i = 0; i < days; i++) {
            dailyViews.put(LocalDate.now().minusDays(days - 1 - i), 0);
        }

        for (Video video : videos) {
            LocalDate date = video.getCreateTime().toLocalDate();
            dailyViews.merge(date, video.getViewCount(), Integer::sum);
        }

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
        }

        List<Video> videos = videoService.list(wrapper);

        int totalLikes = videos.stream().mapToInt(Video::getLikeCount).sum();
        int totalCollects = videos.stream().mapToInt(Video::getCollectCount).sum();

        // 评论数需要查询comment表，这里暂时用0代替或者添加comment service
        int totalComments = 0;

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
