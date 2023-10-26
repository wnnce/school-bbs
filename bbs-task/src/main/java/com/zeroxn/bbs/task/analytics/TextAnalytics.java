package com.zeroxn.bbs.task.analytics;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-19 10:57:27
 * @Description: 文本提词器 提取出字符串中的关键字
 */
public interface TextAnalytics {
    /**
     * 从指定的字符串文本中提取出执行数量的关键字 按照相关度倒叙排序
     * @param text 字符串文本
     * @param size 关键字数量
     * @return 返回提取出的关键字列表
     */
    List<String> keywordsExtraction(String text, int size);
}
