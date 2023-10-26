package com.zeroxn.bbs.task.filter;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 15:52:56
 * @Description: 内容审核接口，提供文本内容审核、图片审核、视频审核
 */
public interface ContentSecurityReview {
    /**
     * 审核文本内容
     * @param text 需要审核的文本字符串
     * @return 审核结果 接口调用失败则null
     */
    Boolean filterText(String text);

    /**
     * 审核图像内容
     * @param imageUrl 需要审核的图片链接
     * @return 审核结果 接口调用失败则null
     */
    Boolean filterImage(String imageUrl);

    /**
     * 审核视频内容
     * @param taskId 本次审核的任务Id
     * @param videoName 视频名称
     * @param videoUrl 视频链接地址
     * @return 审核结果 接口调用失败则null
     */
    Boolean filterVideo(String taskId, String videoName, String videoUrl);
}