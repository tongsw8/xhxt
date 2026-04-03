package lz.xhxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.entity.User;
import lz.xhxt.mapper.UserMapper;
import lz.xhxt.service.JwtService;
import lz.xhxt.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtService jwtService;

    @Override
    public Result login(String account, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getAccount, account));
        if (user == null) {
            return Result.error(ResultCode.USER_LOGIN_ERROR.code(), ResultCode.USER_LOGIN_ERROR.message());
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error(ResultCode.USER_DISABLED.code(), ResultCode.USER_DISABLED.message());
        }
        String md5 = DigestUtils.md5Hex(password);
        if (!md5.equalsIgnoreCase(user.getPassword())) {
            return Result.error(ResultCode.USER_LOGIN_ERROR.code(), ResultCode.USER_LOGIN_ERROR.message());
        }
        String token = jwtService.sign(user.getId(), user.getAccount(), user.getRole());

        Map<String, Object> userVo = new HashMap<>();
        userVo.put("id", user.getId());
        userVo.put("account", user.getAccount());
        userVo.put("nickname", user.getNickname());
        userVo.put("realName", user.getRealName());
        userVo.put("phone", user.getPhone());
        userVo.put("email", user.getEmail());
        userVo.put("gender", user.getGender());
        userVo.put("role", user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userVo);
        return Result.ok(data);
    }

    @Override
    public Result register(String account, String password, String nickname) {
        Integer cnt = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getAccount, account));
        if (cnt != null && cnt > 0) {
            return Result.error(ResultCode.USER_HAS_EXISTED.code(), ResultCode.USER_HAS_EXISTED.message());
        }
        User u = new User();
        u.setAccount(account);
        u.setPassword(DigestUtils.md5Hex(password));
        u.setNickname(nickname != null && !nickname.isEmpty() ? nickname : account);
        u.setRole("USER");
        u.setStatus(1);
        userMapper.insert(u);
        return Result.ok(null);
    }
}
