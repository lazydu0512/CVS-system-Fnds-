package cn.edu.seig.fnds.mapper;

import cn.edu.seig.fnds.entity.CityManager;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 城市管理员Mapper接口
 * 
 * @author lazzydu
 */
@Mapper
public interface CityManagerMapper extends BaseMapper<CityManager> {

    /**
     * 查询用户管理的所有城市
     */
    @Select("SELECT cm.*, c.name as city_name FROM city_manager cm " +
            "LEFT JOIN city c ON cm.city_id = c.id " +
            "WHERE cm.user_id = #{userId}")
    List<CityManager> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询城市的管理员
     */
    @Select("SELECT cm.*, u.nickname as manager_name FROM city_manager cm " +
            "LEFT JOIN user u ON cm.user_id = u.id " +
            "WHERE cm.city_id = #{cityId}")
    CityManager selectByCityId(@Param("cityId") Long cityId);

    /**
     * 根据城市名称查询管理员
     */
    @Select("SELECT cm.*, u.nickname as manager_name FROM city_manager cm " +
            "LEFT JOIN city c ON cm.city_id = c.id " +
            "LEFT JOIN user u ON cm.user_id = u.id " +
            "WHERE c.name = #{cityName}")
    CityManager selectByCityName(@Param("cityName") String cityName);

    /**
     * 查询所有城市管理员及其管理的城市
     */
    @Select("SELECT cm.*, c.name as city_name, u.nickname as manager_name FROM city_manager cm " +
            "LEFT JOIN city c ON cm.city_id = c.id " +
            "LEFT JOIN user u ON cm.user_id = u.id")
    List<CityManager> selectAllWithDetails();

    /**
     * 获取用户管理的城市名称列表
     */
    @Select("SELECT c.name FROM city_manager cm " +
            "LEFT JOIN city c ON cm.city_id = c.id " +
            "WHERE cm.user_id = #{userId}")
    List<String> selectCityNamesByUserId(@Param("userId") Long userId);
}
