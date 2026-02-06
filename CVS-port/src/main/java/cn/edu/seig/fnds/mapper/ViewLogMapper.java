package cn.edu.seig.fnds.mapper;

import cn.edu.seig.fnds.entity.ViewLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频浏览日志Mapper
 * 
 * @author lazzydu
 */
@Mapper
public interface ViewLogMapper extends BaseMapper<ViewLog> {

    /**
     * 统计指定时间范围内的每日浏览量
     */
    @Select("<script>" +
            "SELECT DATE(view_time) as date, COUNT(*) as count " +
            "FROM view_log " +
            "WHERE view_time >= #{startDate} " +
            "<if test='videoIds != null and videoIds.size() > 0'>" +
            "  AND video_id IN " +
            "  <foreach item='id' collection='videoIds' open='(' separator=',' close=')'>" +
            "    #{id}" +
            "  </foreach>" +
            "</if>" +
            "GROUP BY DATE(view_time) " +
            "ORDER BY date" +
            "</script>")
    List<Map<String, Object>> getDailyViewStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("videoIds") List<Long> videoIds);
}
