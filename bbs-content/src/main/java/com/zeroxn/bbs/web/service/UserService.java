package com.zeroxn.bbs.web.service;

import com.zeroxn.bbs.core.entity.User;
import com.zeroxn.bbs.web.dto.UpdateUserDto;

import java.util.Map;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 20:30:43
 * @Description: 用户接口
 */
public interface UserService {
    String login(String code);
    String getUserPhone(String code);
    User queryUserInfo(Long userId);
    void updateUserInfo(Long userId, UpdateUserDto userDto);
    void deleteTopicAfterUpdateUserStars(Integer topicId);
    boolean sendStudentAuthCode(String studentId);
    Map<String, Object> studentAuth(String studentId, String code, Long userId);
}
