package com.zeroxn.bbs.web.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 20:38:01
 * @Description: 文件上传服务层
 */
public interface FileService {
    /**
     * 上级图片文件
     * @param multipartFile 图片文件信息
     * @param md5 文件Md5码
     * @return 返回图片上传后的链接
     */
    String uploadImage(MultipartFile multipartFile, String md5);

    /**
     * 上传视频文件
     * @param multipartFile 视频文件信息
     * @param md5 文件md5码
     * @return 返回视频上传后的链接
     */
    String uploadVideo(MultipartFile multipartFile, String md5);
}
