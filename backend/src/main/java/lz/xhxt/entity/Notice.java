package lz.xhxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("notice")
public class Notice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String coverImg;
    private String content;
    /** 0未发布 1已发布 */
    private Integer status;
    /** 0不置顶 1置顶 */
    private Integer isTop;
    private Date publishTime;
    private Date createTime;
    private Date updateTime;
}