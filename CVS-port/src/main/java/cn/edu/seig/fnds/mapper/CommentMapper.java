package cn.edu.seig.fnds.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.edu.seig.fnds.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论Mapper接口
 * @author lazzydu
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {}