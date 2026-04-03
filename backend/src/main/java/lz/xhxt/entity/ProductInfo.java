package lz.xhxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("product_info")
public class ProductInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String productName;

    private BigDecimal price;

    private Integer stock;

    private String coverImg;

    private String detailText;

    /** 1上架 0下架 */
    private Integer status;

    private Date createTime;

    private Date updateTime;
}
