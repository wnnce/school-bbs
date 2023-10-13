package com.zeroxn.bbs.web.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.zeroxn.bbs.core.common.WechatService;
import com.zeroxn.bbs.core.entity.User;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import com.zeroxn.bbs.web.mapper.UserMapper;
import com.zeroxn.bbs.web.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.zeroxn.bbs.core.entity.table.UserTableDef.USER;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 20:33:17
 * @Description: 用户管理 服务层
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final WechatService wechatService;
    private final UserMapper userMapper;

    public UserServiceImpl(WechatService wechatService, UserMapper userMapper) {
        this.wechatService = wechatService;
        this.userMapper = userMapper;
    }
    @Override
    public String login(String code) {
        String openId = wechatService.getOpenId(code);
        ExceptionUtils.isConditionThrowServer(openId == null, "登录失败，获取OpenId失败");
        User findUser = userMapper.selectOneByQuery(new QueryWrapper().where(USER.OPENID.eq(openId)));
        if (findUser != null) {
            return findUser.getId().toString();
        }
        User user = User.builder().openid(openId).build();
        int result = userMapper.insertSelective(user);
        if (result <= 0) {
            logger.error("保存用户失败");
            ExceptionUtils.throwServerException("登录失败");
        }
        return user.getId().toString();
    }

    @Override
    public String getUserPhone(String code) {
        String phone = wechatService.getPhone(code);
        ExceptionUtils.isConditionThrowServer(phone == null, "获取微信手机号失败");
        return phone;
    }
}
