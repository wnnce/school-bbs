package com.zeroxn.bbs.web.service;

import com.zeroxn.bbs.core.entity.User;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 20:30:43
 * @Description: 用户接口
 */
public interface UserService {
    String login(String code);
    String getUserPhone(String code);
}
