package com.zeroxn.bbs.web.service;

import com.zeroxn.bbs.base.entity.User;
import com.zeroxn.bbs.web.dto.UpdateUserDto;

import java.util.Map;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 20:30:43
 * @Description: 用户接口
 */
public interface UserService {
    /**
     * 使用微信登录的code进行登录
     * @param code 微信登录颁发的code
     * @return 返回登录成功后颁发的token
     */
    String login(String code);

    /**
     * 获取用户手机号
     * @param code 微信获取用户手机号方法拿到的临时code
     * @return 返回用户手机号
     */
    String getUserPhone(String code);

    /**
     * 通过用户Id获取用户的详细信息
     * @param userId 用户Id
     * @return 返回空或者用户详细信息
     */
    User queryUserInfo(Long userId);

    /**
     * 更新用户详细信息
     * @param userId 用户Id
     * @param userDto 新的用户信息
     */
    void updateUserInfo(Long userId, UpdateUserDto userDto);

    /**
     * 通过学号获取学校官网登陆的验证码
     * @param studentId 学号
     * @return 返回验证发送结果
     */
    boolean sendStudentAuthCode(String studentId);

    /**
     * 学生认证
     * @param studentId 学号
     * @param code 学校发送的验证码
     * @param userId 需要学生认证的用户Id
     * @return 认证成功返回学生信息，失败返回null
     */
    Map<String, Object> studentAuth(String studentId, String code, Long userId);
}
