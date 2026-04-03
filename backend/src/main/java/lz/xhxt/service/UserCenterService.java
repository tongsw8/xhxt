package lz.xhxt.service;

import lz.xhxt.common.Result;
import lz.xhxt.dto.AddressSaveRequest;
import lz.xhxt.dto.ChangePasswordRequest;
import lz.xhxt.dto.UpdateProfileRequest;

public interface UserCenterService {

    Result getProfile(Long userId);

    Result updateProfile(Long userId, UpdateProfileRequest req);

    Result changePassword(Long userId, ChangePasswordRequest req);

    Result listAddress(Long userId);

    Result saveAddress(Long userId, AddressSaveRequest req);

    Result deleteAddress(Long userId, Long id);

    Result setDefaultAddress(Long userId, Long id);
}
