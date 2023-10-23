package com.zeroxn.bbs.core.filter;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 15:52:56
 * @Description: 内容审核接口，提供文本内容审核、图片审核、视频审核
 */
public interface ContentSecurityReview {
    String filterText(String text);
    boolean filterImage(String imageUrl);
    boolean filterVideo(String taskId, String videoName, String videoUrl);
}