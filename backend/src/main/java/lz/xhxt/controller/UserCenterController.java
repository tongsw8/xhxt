package lz.xhxt.controller;

import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.dto.AddressSaveRequest;
import lz.xhxt.dto.ChangePasswordRequest;
import lz.xhxt.dto.UpdateProfileRequest;
import lz.xhxt.service.JwtService;
import lz.xhxt.service.UserCenterService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/user/profile")
public class UserCenterController {

    @Resource
    private UserCenterService userCenterService;

    @Resource
    private JwtService jwtService;

    @GetMapping("/info")
    public Result getInfo(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return userCenterService.getProfile(userId);
    }

    @PostMapping("/update")
    public Result update(@RequestHeader(value = "Authorization", required = false) String authorization,
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

    @GetMapping("/address/list")
    public Result listAddress(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return userCenterService.listAddress(userId);
    }

    @PostMapping("/address/save")
    public Result saveAddress(@RequestHeader(value = "Authorization", required = false) String authorization,
                              @RequestBody AddressSaveRequest req) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return userCenterService.saveAddress(userId, req);
    }

    @DeleteMapping("/address/delete/{id}")
    public Result deleteAddress(@RequestHeader(value = "Authorization", required = false) String authorization,
                                @PathVariable Long id) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return userCenterService.deleteAddress(userId, id);
    }

    @PostMapping("/address/default/{id}")
    public Result setDefault(@RequestHeader(value = "Authorization", required = false) String authorization,
                             @PathVariable Long id) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return userCenterService.setDefaultAddress(userId, id);
    }

    private Long getUserIdFromToken(String authorization) {
        try {
            return jwtService.parseUserId(authorization);
        } catch (Exception e) {
            return null;
        }
    }
}
