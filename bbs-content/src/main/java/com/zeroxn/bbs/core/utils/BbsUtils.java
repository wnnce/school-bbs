package com.zeroxn.bbs.core.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 18:15:42
 * @Description:
 */
public final class BbsUtils {

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
}
