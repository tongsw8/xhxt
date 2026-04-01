package lz.xhxt.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private String realName;
    private String phone;
    private String email;
    private Integer gender;
}
