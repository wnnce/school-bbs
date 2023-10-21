package com.zeroxn.bbs.core.config.security;

import com.zeroxn.bbs.base.entity.User;
import com.zeroxn.bbs.web.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 18:52:30
 * @Description: 自定义Token有效性验证类
 */
public class UserTokenValidator implements OAuth2TokenValidator<Jwt> {
    private final Logger logger = LoggerFactory.getLogger(UserTokenValidator.class);
    private final UserService userService;

    public UserTokenValidator(UserService userService) {
        this.userService = userService;
    }

    /**
     * 自定义Token的查询逻辑，判断当前Token的是否有效，以及Token中的信息和数据库中是否一致
     * @param jwt 请求传入的Token
     * @return 返回验证成功或失败
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Long userId = null;
        try{
            userId = Long.parseLong(jwt.getId());
        }catch (Exception e) {
            logger.error("Token解析用户ID失败，TokenId:{}", jwt.getId());
            return OAuth2TokenValidatorResult.failure(makeOAuth2Error("Token格式错误"));
        }
        User findUser = userService.queryUserInfo(userId);
        if (findUser == null) {
            logger.error("Token无效，查询用户为空，userId：{}", userId);
            return OAuth2TokenValidatorResult.failure(makeOAuth2Error("Token已过期"));
        }
        if (findUser.getUserAuth() == 1) {
            logger.error("Token用户已被禁用，userId:{}", userId);
            return OAuth2TokenValidatorResult.failure(makeOAuth2Error("用户被禁用"));
        }
        List<String> scope = jwt.getClaimAsStringList("scope");
        if (scope == null || scope.isEmpty()) {
            logger.error("Token权限为空");
            return OAuth2TokenValidatorResult.failure(makeOAuth2Error("Token权限错误"));
        }
        int scopeSize = findUser.getRole() == 0 ? 1 : 2;
        if (scope.size() == scopeSize) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(makeOAuth2Error("Token权限错误"));
    }
    private OAuth2Error makeOAuth2Error(String message) {
        return new OAuth2Error("401", message, null);
    }
}
