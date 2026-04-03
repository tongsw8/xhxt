package lz.xhxt.controller;

import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.dto.ChangePasswordRequest;
import lz.xhxt.dto.UpdateProfileRequest;
import lz.xhxt.service.JwtService;
import lz.xhxt.service.UserCenterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/user/center")
public class UserCenterAliasController {

    @Resource
    private UserCenterService userCenterService;

    @Resource
    private JwtService jwtService;

    @GetMapping("/profile")
    public Result getProfile(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return userCenterService.getProfile(userId);
    }

    @PostMapping("/profile/update")
    public Result updateProfile(@RequestHeader(value = "Authorization", required = false) String authorization,
                                @RequestBody UpdateProfileRequest req) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return userCenterService.updateProfile(userId, req);
    }

    @PostMapping("/password")
    public Result changePassword(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @RequestBody ChangePasswordRequest req) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return userCenterService.changePassword(userId, req);
    }

    private Long getUserIdFromToken(String authorization) {
        try {
            return jwtService.parseUserId(authorization);
        } catch (Exception e) {
            return null;
        }
    }
}