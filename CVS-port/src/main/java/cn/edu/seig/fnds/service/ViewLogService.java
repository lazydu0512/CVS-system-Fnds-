package cn.edu.seig.fnds.service;

import cn.edu.seig.fnds.entity.ViewLog;
import cn.edu.seig.fnds.mapper.ViewLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频浏览日志服务类
 * 
 * @author lazzydu
 */
@Service
public class ViewLogService extends ServiceImpl<ViewLogMapper, ViewLog> {

    @Autowired
    private VideoService videoService;

    /**
     * 记录视频浏览
     */
    public boolean recordView(Long videoId, Long userId, String ipAddress) {
        ViewLog log = new ViewLog();
        log.setVideoId(videoId);
        log.setUserId(userId);
        log.setIpAddress(ipAddress);
        log.setViewTime(LocalDateTime.now());

        boolean saved = save(log);

        // 更新视频表的浏览计数
        if (saved) {
            videoService.incrementViewCount(videoId);
        }

        return saved;
    }

    /**
     * 获取指定时间范围内的每日浏览统计
     * 
     * @param startDate 开始日期
     * @param videoIds  视频ID列表，为null表示统计所有视频
     */
    public List<Map<String, Object>> getDailyViewStats(LocalDateTime startDate, List<Long> videoIds) {
        return baseMapper.getDailyViewStats(startDate, videoIds);
    }
}
