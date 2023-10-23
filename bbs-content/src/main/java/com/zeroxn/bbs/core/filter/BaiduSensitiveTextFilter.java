package com.zeroxn.bbs.core.filter;

import com.zeroxn.bbs.core.common.BaiduService;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 13:17:42
 * @Description: 基于百度AI敏感词检测的敏感词过滤器
 */
public class BaiduSensitiveTextFilter implements SensitiveTextFilter {
    private static final Logger logger = LoggerFactory.getLogger(BaiduSensitiveTextFilter.class);
    private final BaiduService baiduService;
    private BaiduSensitiveTextFilter(BaiduService baiduService) {
        this.baiduService = baiduService;
    }

    @Override
    public String filterText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        BaiduService.ReviewResult result = baiduService.textReview(text);
        ExceptionUtils.isConditionThrowRequest(result == null, "内容审核失败，请重试");
        if (result.conclusionType() == 1) {
            return null;
        }
        List<String> words = result.data().get(0).hits().get(0).words();
        String word = null;
        if (words.isEmpty()) {
            word = result.data().get(0).msg().replace("存在", "").replace("不合规", "");
        }else {
            word = words.get(0);
        }
        return word;
    }
}
