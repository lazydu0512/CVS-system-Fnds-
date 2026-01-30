package cn.edu.seig.fnds.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.edu.seig.fnds.entity.VideoLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频点赞Mapper接口
 * @author lazzydu
 */
@Mapper
public interface VideoLikeMapper extends BaseMapper<VideoLike> {}