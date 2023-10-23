package com.zeroxn.bbs.core.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.zeroxn.bbs.core.common.BaiduService;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 17:57:24
 * @Description:
 */
public class BaiduContentSecurityReview implements ContentSecurityReview {
    private static final Logger logger = LoggerFactory.getLogger(BaiduContentSecurityReview.class);
    private final BaiduService baiduService;
    private final SensitiveTextFilter textFilter;

    public BaiduContentSecurityReview(BaiduService baiduService, SensitiveTextFilter textFilter) {
        this.baiduService = baiduService;
        this.textFilter = textFilter;
    }
    @Override
    public String filterText(String text) {
        return textFilter.filterText(text);
    }

    @Override
    public boolean filterImage(String imageUrl) {
        JsonNode jsonNode = baiduService.imageReview(imageUrl);
        ExceptionUtils.isConditionThrowRequest(jsonNode == null, "图片审核失败，请重试");
        if (!jsonNode.path("error_code").isEmpty()) {
            logger.error("图片审核失败，错误码：{},错误消息：{}", jsonNode.path("error_code").toString(), jsonNode.path("error_msg").toString());
        }
        return jsonNode.path("conclusionType").asInt() == 1;
    }

    @Override
    public boolean filterVideo(String taskId, String videoName, String videoUrl) {
        JsonNode jsonNode = baiduService.videoReview(taskId, videoName, videoUrl);
        ExceptionUtils.isConditionThrowRequest(jsonNode == null, "视频审核失败，请重试");
        if (!jsonNode.path("error_code").isEmpty()) {
            logger.error("视频审核失败，错误码：{},错误消息：{}", jsonNode.path("error_code").toString(), jsonNode.path("error_msg").toString());
        }
        return jsonNode.path("conclusionType").asInt() == 1;
    }
}
