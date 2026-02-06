package cn.edu.seig.fnds.mapper;

import cn.edu.seig.fnds.entity.Report;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 举报记录Mapper接口
 * 
 * @author lazzydu
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}
