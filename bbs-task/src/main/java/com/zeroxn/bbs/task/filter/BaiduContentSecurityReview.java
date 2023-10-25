package com.zeroxn.bbs.task.filter;

import com.fasterxml.jackson.databind.JsonNode;
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
    public BaiduContentSecurityReview(BaiduService baiduService) {
        this.baiduService = baiduService;
    }
    @Override
    public Boolean filterText(String text) {
        BaiduService.ReviewResult reviewResult = baiduService.textReview(text);
        if (reviewResult == null) {
            logger.error("文本审核调用失败");
            return null;
        }
        return reviewResult.conclusionType() == 1;
    }

    @Override
    public Boolean filterImage(String imageUrl) {
        JsonNode jsonNode = baiduService.imageReview(imageUrl);
        if (jsonNode == null) {
            logger.error("图片审核调用失败");
            return null;
        }
        if (!jsonNode.get("error_code").isEmpty()) {
            logger.error("图片审核失败，错误码：{},错误消息：{}", jsonNode.path("error_code").toString(), jsonNode.path("error_msg").toString());
            return null;
        }
        return jsonNode.path("conclusionType").asInt() == 1;
    }

    @Override
    public Boolean filterVideo(String taskId, String videoName, String videoUrl) {
        JsonNode jsonNode = baiduService.videoReview(taskId, videoName, videoUrl);
        if (jsonNode == null) {
            logger.error("视频审核调用失败");
            return null;
        }
        if (!jsonNode.get("error_code").isEmpty()) {
            logger.error("视频审核失败，错误码：{},错误消息：{}", jsonNode.path("error_code").toString(), jsonNode.path("error_msg").toString());
            return null;
        }
        return jsonNode.path("conclusionType").asInt() == 1;
    }
}
