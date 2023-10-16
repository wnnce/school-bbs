package com.zeroxn.bbs.web.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import com.zeroxn.bbs.core.common.JwtService;
import com.zeroxn.bbs.core.common.StudentAuthService;
import com.zeroxn.bbs.core.common.WechatService;
import com.zeroxn.bbs.core.entity.Student;
import com.zeroxn.bbs.core.entity.User;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import com.zeroxn.bbs.web.dto.UpdateUserDto;
import com.zeroxn.bbs.web.mapper.StudentMapper;
import com.zeroxn.bbs.web.mapper.UserExtrasMapper;
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

import static com.zeroxn.bbs.core.entity.table.UserTableDef.USER;
import static com.zeroxn.bbs.core.entity.table.StudentTableDef.STUDENT;

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
    private final UserExtrasMapper extrasMapper;
    private final StudentMapper studentMapper;
    private final GlobalAsyncTask asyncTask;


    public UserServiceImpl(WechatService wechatService, UserMapper userMapper, JwtService jwtService,
                           StudentMapper studentMapper, StudentAuthService authService, GlobalAsyncTask asyncTask,
                           UserExtrasMapper extrasMapper) {
        this.wechatService = wechatService;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.studentMapper = studentMapper;
        this.authService = authService;
        this.asyncTask = asyncTask;
        this.extrasMapper = extrasMapper;
    }
    @Override
    public String login(String openId) {
        // TODO 临时禁用OpenId获取 用于测试
//        String openId = wechatService.getOpenId(code);
//        ExceptionUtils.isConditionThrowServer(openId == null, "登录失败，获取OpenId失败");
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

    @Override
    public String getUserPhone(String code) {
        String phone = wechatService.getPhone(code);
        ExceptionUtils.isConditionThrowServer(phone == null, "获取微信手机号失败");
        return phone;
    }

    @Override
    public User queryUserInfo(Long userId) {
        return userMapper.selectOneByQuery(new QueryWrapper().where(USER.ID.eq(userId)).where(USER.STATUS.ne(2)));
    }

    @Override
    public void updateUserInfo(Long userId, UpdateUserDto userDto) {
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        user.setId(userId);
        userMapper.update(user);
    }

    @Override
    public void deleteTopicAfterUpdateUserStars(Integer topicId) {
        int result = extrasMapper.deleteTopicAfterUpdateUserStars(topicId);
        logger.info("删除帖子后移除用户收藏，TopicId:{}，受影响的用户数：{}", topicId, result);
    }

    @Override
    public boolean sendStudentAuthCode(String studentId) {
        return authService.sendStudentLoginCode(studentId);
    }

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

    private void updateUserRole(Long userId, Integer newRole) {
        User user = UpdateEntity.of(User.class, userId);
        user.setRole(newRole);
        userMapper.update(user);
    }
}
