package lz.xhxt.service;

import lz.xhxt.common.Result;

public interface UserService {

    Result login(String account, String password);

    Result register(String account, String password, String nickname);
}
