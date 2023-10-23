package com.zeroxn.bbs.core.filter;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 13:15:22
 * @Description: 敏感词过滤接口
 */
public interface SensitiveTextFilter {
    String filterText(String text);
}
