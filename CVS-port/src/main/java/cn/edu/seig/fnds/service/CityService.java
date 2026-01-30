package cn.edu.seig.fnds.service;

import cn.edu.seig.fnds.entity.City;
import cn.edu.seig.fnds.mapper.CityMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 城市服务类
 * 
 * @author lazzydu
 */
@Service
public class CityService extends ServiceImpl<CityMapper, City> {

    /**
     * 获取所有城市（按创建时间降序排序）
     */
    public List<City> getAllCities() {
        return list(new LambdaQueryWrapper<City>()
                .orderByDesc(City::getCreateTime));
    }

    /**
     * 检查城市名是否存在
     */
    public boolean checkNameExists(String name, Long excludeId) {
        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<City>()
                .eq(City::getName, name);
        if (excludeId != null) {
            wrapper.ne(City::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
