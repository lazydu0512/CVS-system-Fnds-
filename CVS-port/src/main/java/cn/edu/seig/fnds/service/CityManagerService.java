package cn.edu.seig.fnds.service;

import cn.edu.seig.fnds.entity.City;
import cn.edu.seig.fnds.entity.CityManager;
import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.mapper.CityManagerMapper;
import cn.edu.seig.fnds.mapper.CityMapper;
import cn.edu.seig.fnds.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 城市管理员服务类
 * 
 * @author lazzydu
 */
@Service
public class CityManagerService extends ServiceImpl<CityManagerMapper, CityManager> {

    private final CityManagerMapper cityManagerMapper;
    private final UserMapper userMapper;
    private final CityMapper cityMapper;

    public CityManagerService(CityManagerMapper cityManagerMapper, UserMapper userMapper, CityMapper cityMapper) {
        this.cityManagerMapper = cityManagerMapper;
        this.userMapper = userMapper;
        this.cityMapper = cityMapper;
    }

    /**
     * 获取用户管理的城市列表
     */
    public List<CityManager> getManagedCities(Long userId) {
        return cityManagerMapper.selectByUserId(userId);
    }

    /**
     * 获取用户管理的城市名称列表
     */
    public List<String> getManagedCityNames(Long userId) {
        return cityManagerMapper.selectCityNamesByUserId(userId);
    }

    /**
     * 获取城市的管理员
     */
    public CityManager getCityManager(Long cityId) {
        return cityManagerMapper.selectByCityId(cityId);
    }

    /**
     * 根据城市名称获取管理员
     */
    public CityManager getCityManagerByName(String cityName) {
        return cityManagerMapper.selectByCityName(cityName);
    }

    /**
     * 分配城市给管理员
     * 
     * @param userId 城市管理员用户ID
     * @param cityId 城市ID
     * @return 操作结果消息
     */
    @Transactional
    public String assignCity(Long userId, Long cityId) {
        // 验证用户存在且是城市管理员
        User user = userMapper.selectById(userId);
        if (user == null) {
            return "用户不存在";
        }
        if (user.getRole() != 2) {
            return "该用户不是城市管理员";
        }

        // 验证城市存在
        City city = cityMapper.selectById(cityId);
        if (city == null) {
            return "城市不存在";
        }

        // 检查城市是否已被分配
        CityManager existing = cityManagerMapper.selectByCityId(cityId);
        if (existing != null) {
            if (existing.getUserId().equals(userId)) {
                return "该城市已分配给此管理员";
            }
            return "该城市已被其他管理员管理";
        }

        // 创建关联
        CityManager cityManager = new CityManager();
        cityManager.setUserId(userId);
        cityManager.setCityId(cityId);
        cityManagerMapper.insert(cityManager);

        return null; // null 表示成功
    }

    /**
     * 移除管理员的城市权限
     */
    @Transactional
    public String removeCity(Long userId, Long cityId) {
        QueryWrapper<CityManager> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("city_id", cityId);
        int deleted = cityManagerMapper.delete(wrapper);
        if (deleted == 0) {
            return "未找到对应的管理权限";
        }
        return null;
    }

    /**
     * 判断用户是否有权管理某城市的视频
     */
    public boolean canManageVideo(Long userId, String city) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        // 系统管理员可以管理所有视频
        if (user.getRole() == 1) {
            return true;
        }

        // 城市管理员只能管理其负责的城市
        if (user.getRole() == 2) {
            List<String> managedCities = getManagedCityNames(userId);
            return managedCities.contains(city);
        }

        return false;
    }

    /**
     * 获取所有城市管理员及其管理的城市
     */
    public List<CityManager> getAllCityManagersWithDetails() {
        return cityManagerMapper.selectAllWithDetails();
    }

    /**
     * 获取所有城市管理员用户列表
     */
    public List<User> getAllCityManagerUsers() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", 2);
        return userMapper.selectList(wrapper);
    }

    /**
     * 设置用户为城市管理员
     */
    @Transactional
    public String setUserAsCityManager(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return "用户不存在";
        }
        if (user.getRole() == 1) {
            return "不能将系统管理员设置为城市管理员";
        }
        user.setRole(2);
        userMapper.updateById(user);
        return null;
    }

    /**
     * 取消用户的城市管理员角色
     */
    @Transactional
    public String removeCityManagerRole(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return "用户不存在";
        }
        if (user.getRole() != 2) {
            return "该用户不是城市管理员";
        }

        // 先移除所有管理的城市
        QueryWrapper<CityManager> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        cityManagerMapper.delete(wrapper);

        // 设置为普通用户
        user.setRole(0);
        userMapper.updateById(user);
        return null;
    }

    /**
     * 根据城市名称获取该城市的管理员列表
     */
    public List<CityManager> getManagersByCity(String cityName) {
        return cityManagerMapper.selectManagersByCityName(cityName);
    }
}
