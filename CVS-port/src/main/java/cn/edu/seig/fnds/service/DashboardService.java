package cn.edu.seig.fnds.service;

import java.util.List;
import java.util.Map;

/**
 * Dashboard服务接口
 * 
 * @author lazzydu
 */
public interface DashboardService {

    /**
     * 获取总览数据
     * 
     * @param userId 用户ID（城市管理员需要）
     * @param role   用户角色
     * @return 总览数据
     */
    Map<String, Object> getOverview(Long userId, Integer role);

    /**
     * 获取视频浏览量趋势
     * 
     * @param days   天数
     * @param userId 用户ID
     * @param role   用户角色
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getVideoViewsTrend(Integer days, Long userId, Integer role);

    /**
     * 获取用户注册量趋势
     * 
     * @param days 天数
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getUserRegisterTrend(Integer days);

    /**
     * 获取视频上传量趋势
     * 
     * @param days   天数
     * @param userId 用户ID
     * @param role   用户角色
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getVideoUploadTrend(Integer days, Long userId, Integer role);

    /**
     * 获取互动数据统计
     * 
     * @param userId 用户ID
     * @param role   用户角色
     * @return 互动数据统计
     */
    Map<String, Object> getInteractionStats(Long userId, Integer role);

    /**
     * 获取城市视频分布
     * 
     * @param userId 用户ID
     * @param role   用户角色
     * @return 城市分布数据
     */
    List<Map<String, Object>> getCityDistribution(Long userId, Integer role);

    /**
     * 获取热门视频排行
     * 
     * @param limit  数量限制
     * @param userId 用户ID
     * @param role   用户角色
     * @return 热门视频列表
     */
    List<Map<String, Object>> getTopVideos(Integer limit, Long userId, Integer role);
}
