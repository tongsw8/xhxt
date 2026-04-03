package lz.xhxt.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String account;

    private String password;

    /** 可选，不传则默认用账号 */
    private String nickname;
}
