package com.zeroxn.bbs.web.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 20:38:01
 * @Description: 文件上传服务层
 */
public interface FileService {
    String uploadImage(MultipartFile multipartFile, String md5);
    String uploadVideo(MultipartFile multipartFile, String md5);
}
