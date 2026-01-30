package cn.edu.seig.fnds.mapper;

import cn.edu.seig.fnds.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知Mapper接口
 * 
 * @author lazzydu
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
