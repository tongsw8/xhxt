package lz.xhxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lz.xhxt.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}