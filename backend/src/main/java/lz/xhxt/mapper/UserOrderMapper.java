package lz.xhxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lz.xhxt.entity.UserOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserOrderMapper extends BaseMapper<UserOrder> {
}
