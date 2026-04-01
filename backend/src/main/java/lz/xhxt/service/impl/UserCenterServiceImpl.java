package lz.xhxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.dto.AddressSaveRequest;
import lz.xhxt.dto.ChangePasswordRequest;
import lz.xhxt.dto.UpdateProfileRequest;
import lz.xhxt.entity.User;
import lz.xhxt.entity.UserAddress;
import lz.xhxt.mapper.UserAddressMapper;
import lz.xhxt.mapper.UserMapper;
import lz.xhxt.service.UserCenterService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class UserCenterServiceImpl implements UserCenterService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserAddressMapper userAddressMapper;

    @Override
    public Result getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "用户不存在");
        user.setPassword(null);
        return Result.ok(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateProfile(Long userId, UpdateProfileRequest req) {
        if (req == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        User user = userMapper.selectById(userId);
        if (user == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "用户不存在");
        user.setNickname(req.getNickname());
        user.setRealName(req.getRealName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setGender(req.getGender());
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
        return Result.ok(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result changePassword(Long userId, ChangePasswordRequest req) {
        if (req == null || req.getOldPassword() == null || req.getNewPassword() == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        User user = userMapper.selectById(userId);
        if (user == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "用户不存在");
        if (!DigestUtils.md5Hex(req.getOldPassword()).equalsIgnoreCase(user.getPassword())) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "旧密码不正确");
        }
        user.setPassword(DigestUtils.md5Hex(req.getNewPassword()));
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
        return Result.ok(null);
    }

    @Override
    public Result listAddress(Long userId) {
        List<UserAddress> list = userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getId));
        return Result.ok(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveAddress(Long userId, AddressSaveRequest req) {
        if (req == null || req.getReceiverName() == null || req.getReceiverPhone() == null || req.getDetailAddress() == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "收货地址信息不完整");
        }
        UserAddress a;
        if (req.getId() == null) {
            a = new UserAddress();
            a.setUserId(userId);
            a.setCreateTime(new Date());
        } else {
            a = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getId, req.getId())
                    .eq(UserAddress::getUserId, userId));
            if (a == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "地址不存在");
        }

        a.setReceiverName(req.getReceiverName());
        a.setReceiverPhone(req.getReceiverPhone());
        a.setProvince(req.getProvince());
        a.setCity(req.getCity());
        a.setDistrict(req.getDistrict());
        a.setDetailAddress(req.getDetailAddress());
        a.setIsDefault(req.getIsDefault() != null && req.getIsDefault() == 1 ? 1 : 0);
        a.setUpdateTime(new Date());

        if (a.getIsDefault() == 1) {
            clearDefault(userId);
        }

        if (req.getId() == null) userAddressMapper.insert(a);
        else userAddressMapper.updateById(a);

        return Result.ok(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteAddress(Long userId, Long id) {
        userAddressMapper.delete(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getId, id));
        List<UserAddress> list = userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getId));
        boolean hasDefault = list.stream().anyMatch(x -> x.getIsDefault() != null && x.getIsDefault() == 1);
        if (!hasDefault && !list.isEmpty()) {
            UserAddress first = list.get(0);
            first.setIsDefault(1);
            first.setUpdateTime(new Date());
            userAddressMapper.updateById(first);
        }
        return Result.ok(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result setDefaultAddress(Long userId, Long id) {
        UserAddress a = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getId, id)
                .eq(UserAddress::getUserId, userId));
        if (a == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "地址不存在");
        clearDefault(userId);
        a.setIsDefault(1);
        a.setUpdateTime(new Date());
        userAddressMapper.updateById(a);
        return Result.ok(null);
    }

    private void clearDefault(Long userId) {
        List<UserAddress> list = userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1));
        for (UserAddress x : list) {
            x.setIsDefault(0);
            x.setUpdateTime(new Date());
            userAddressMapper.updateById(x);
        }
    }
}
