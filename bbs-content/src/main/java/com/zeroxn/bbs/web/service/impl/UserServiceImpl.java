package com.zeroxn.bbs.web.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.zeroxn.bbs.base.entity.Student;
import com.zeroxn.bbs.base.entity.User;
import com.zeroxn.bbs.core.common.JwtService;
import com.zeroxn.bbs.core.common.StudentAuthService;
import com.zeroxn.bbs.core.common.WechatService;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import com.zeroxn.bbs.web.dto.UpdateUserDto;
import com.zeroxn.bbs.web.mapper.StudentMapper;
import com.zeroxn.bbs.web.mapper.UserMapper;
import com.zeroxn.bbs.web.service.UserService;
import com.zeroxn.bbs.web.service.async.GlobalAsyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

import static com.zeroxn.bbs.base.entity.table.StudentTableDef.STUDENT;
import static com.zeroxn.bbs.base.entity.table.UserTableDef.USER;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 20:33:17
 * @Description: 用户管理 服务层
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final WechatService wechatService;
    private final JwtService jwtService;
    private final StudentAuthService authService;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final GlobalAsyncTask asyncTask;


    public UserServiceImpl(WechatService wechatService, UserMapper userMapper, JwtService jwtService,
                           StudentMapper studentMapper, StudentAuthService authService, GlobalAsyncTask asyncTask) {
        this.wechatService = wechatService;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.studentMapper = studentMapper;
        this.authService = authService;
        this.asyncTask = asyncTask;
    }

    /**
     * 先通过code获取微信的OpenId,再判断数据库中openid是否存在，如果存在直接判断用户角色，生成对应的Token
     * 如果不存在，那么将获取到的openid保存到数据库，然后颁发user权限的Token
     * @param code 微信登录颁发的code
     * @return 返回生成的Token
     */
    @Override
    public String login(String code) {
        String openId = wechatService.getOpenId(code);
        ExceptionUtils.isConditionThrowServer(openId == null, "登录失败，获取OpenId失败");
        User findUser = userMapper.selectOneByQuery(new QueryWrapper().where(USER.OPENID.eq(openId)));
        if (findUser != null) {
            if (findUser.getUserAuth() == 1) {
                logger.warn("用户被禁用，禁止登陆，userId：{}", findUser.getId());
                ExceptionUtils.throwRequestException("当前用户已被禁用");
            }
            Set<String> userScope = null;
            if (findUser.getRole() == 1) {
                userScope = Set.of("user", "student");
            }else {
                userScope = Set.of("user");
            }
            return jwtService.generateToken(findUser.getId().toString(), findUser.getOpenid(), userScope);
        }
        User user = User.builder().openid(openId).build();
        int result = userMapper.insertSelective(user);
        if (result <= 0) {
            logger.error("保存用户失败");
            ExceptionUtils.throwServerException("登录失败");
        }
        // 异步初始化用户的额外信息
        asyncTask.initUserExtraInit(user.getId());
        return jwtService.generateToken(user.getId().toString(), user.getOpenid(), Set.of("user"));
    }

    /**
     * 调用微信接口，通过code获取用户的手机号
     * @param code 微信获取用户手机号方法拿到的临时code
     * @return 返回用户的手机号
     */
    @Override
    public String getUserPhone(String code) {
        String phone = wechatService.getPhone(code);
        ExceptionUtils.isConditionThrowServer(phone == null, "获取微信手机号失败");
        return phone;
    }

    /**
     *
     * @param userId 用户Id
     * @return 返回空或者用户详细信息
     */
    @Override
    public User queryUserInfo(Long userId) {
        return userMapper.selectOneByQuery(new QueryWrapper()
                .where(USER.ID.eq(userId))
                .and(USER.USER_AUTH.eq(0)));
    }

    /**
     * 更新用户信息
     * @param userId 用户Id
     * @param userDto 新的用户信息
     */
    @Override
    public void updateUserInfo(Long userId, UpdateUserDto userDto) {
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        user.setId(userId);
        userMapper.update(user);
    }

    @Override
    public boolean sendStudentAuthCode(String studentId) {
        return authService.sendStudentLoginCode(studentId);
    }

    /**
     * 获取学生信息后，先判断当前用户是否已经学生认证，再判断当前学号是否在数据库中存在
     * 只有条件都符合，才将学生信息写入数据库
     * @param studentId 学号
     * @param code 学校发送的验证码
     * @param userId 需要学生认证的用户Id
     * @return 返回新的Token和学生信息
     */
    @Override
    @Transactional
    public Map<String, Object> studentAuth(String studentId, String code, Long userId) {
        User findUser = userMapper.selectOneById(userId);
        ExceptionUtils.isConditionThrowRequest(findUser.getRole() == 1 , "当前用户已经认证，请勿重复认证");
        Student student = authService.getStudentInfo(studentId, code);
        student.setUserId(userId);
        long studentCount = studentMapper.selectCountByQuery(new QueryWrapper().where(STUDENT.STUDENT_ID.eq(student.getStudentId())));
        ExceptionUtils.isConditionThrowRequest(studentCount > 0, "当前学号已被认证");
        int result = studentMapper.insertSelective(student);
        ExceptionUtils.isConditionThrowServer(result <= 0, "学生认证失败，请重试");
        this.updateUserRole(userId, 1);
        String token = jwtService.generateToken(userId.toString(), findUser.getOpenid(), Set.of("user", "student"));
        return Map.of("info", student, "token", token);
    }

    /**
     * 私有方法 更新用户角色
     * @param userId 用户id
     * @param newRole 新的用户角色
     */
    private void updateUserRole(Long userId, Integer newRole) {
        User user = UpdateEntity.of(User.class, userId);
        user.setRole(newRole);
        userMapper.update(user);
    }
}
