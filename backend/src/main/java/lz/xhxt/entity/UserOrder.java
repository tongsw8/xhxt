package lz.xhxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("user_order")
public class UserOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    /** 0待支付 1已支付待发货 2已发货待收货 3已完成 4已关闭 */
    private Integer status;

    private Date expireTime;

    private Date payTime;

    private Date deliveryTime;

    private Date finishTime;

    private Date closeTime;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String cardMessage;

    private String deliveryExpectTime;

    private Integer notifyReadAdmin;

    private Integer notifyReadStaff;

    private Integer urgeDelivery;

    private String expressCompany;

    private String trackingNo;

    private Date createTime;

    private Date updateTime;
}