package com.zeroxn.bbs.task.analytics;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-19 10:57:27
 * @Description:
 */
public interface TextAnalytics {
    List<String> keywordsExtraction(String text, int size);
}
