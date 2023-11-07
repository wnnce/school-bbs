package com.zeroxn.bbs.core.util;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 18:15:42
 * @Description: 项目工具类
 */
public final class BbsUtils {
    private static final Logger logger = LoggerFactory.getLogger(BbsUtils.class);

    /**
     * 获取请求的具体Ip地址
     * @param request http请求对象
     * @return 返回请求的IP地址
     */
    public static String getRequestIpAddress(HttpServletRequest request) {
        String address = request.getHeader("X-Forwarded-For");
        if (address == null || address.trim().isEmpty() || "unknown".equalsIgnoreCase(address)) {
            address = request.getHeader("X-Real-IP");
        }
        if (address == null || address.trim().isEmpty() || "unknown".equalsIgnoreCase(address)) {
            address = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(address)) {
            address = "127.0.0.1";
        }
        int index = address.indexOf(',');
        if (index != -1) {
            address = address.substring(0, index);
        }
        return address;
    }

    /**
     * 从Token Jwt对象中获取userId
     * @param jwt 请求的Token对象
     * @return 返回解析得到的用户Id
     */
    public static Long formJwtGetUserId(Jwt jwt) {
        Long userId = null;
        try {
            userId = Long.valueOf(jwt.getId());
        }catch (Exception e) {
            logger.error("Token获取用户ID失败，subject:{}", jwt.getSubject());
        }
        return userId;
    }
}
