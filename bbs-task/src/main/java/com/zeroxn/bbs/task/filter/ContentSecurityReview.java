package com.zeroxn.bbs.task.filter;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 15:52:56
 * @Description: 内容审核接口，提供文本内容审核、图片审核、视频审核
 */
public interface ContentSecurityReview {
    Boolean filterText(String text);
    Boolean filterImage(String imageUrl);
    Boolean filterVideo(String taskId, String videoName, String videoUrl);
}