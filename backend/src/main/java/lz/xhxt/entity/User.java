package lz.xhxt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String account;

    private String password;

    private String nickname;

    private String realName;

    private String phone;

    private String email;

    /** 0未知 1男 2女 */
    private Integer gender;

    /** ADMIN / STAFF / USER */
    private String role;

    /** 1 正常 0 禁用 */
    private Integer status;

    private Date regTime;

    private Date updateTime;
}
