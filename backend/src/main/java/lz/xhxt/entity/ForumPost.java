package lz.xhxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("forum_post")
public class ForumPost {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private Long userId;
    private Long productId;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer isTop;
    private Integer isBest;
    /** 1可见 0不可见 */
    private Integer status;
    private Date createTime;
    private Date updateTime;
}