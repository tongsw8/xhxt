package lz.xhxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long targetId;
    private String targetType;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer isStaff;
    private Integer status;
    private Date createTime;
}