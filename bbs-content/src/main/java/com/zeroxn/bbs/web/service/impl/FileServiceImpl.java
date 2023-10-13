package com.zeroxn.bbs.web.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.zeroxn.bbs.core.common.QiniuBucketService;
import com.zeroxn.bbs.core.config.qiniu.QiniuProperties;
import com.zeroxn.bbs.core.exception.CustomException;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import com.zeroxn.bbs.web.mapper.FileUploadMapper;
import com.zeroxn.bbs.web.service.FileService;
import com.zeroxn.bbs.web.service.async.GlobalAsyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 20:39:15
 * @Description: 文件上传服务层实现类
 */
@Service
public class FileServiceImpl extends QiniuBucketService implements FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    private final QiniuProperties properties;
    private final GlobalAsyncTask asyncTask;
    public FileServiceImpl(Auth auth, QiniuProperties properties, UploadManager uploadManager, BucketManager bucketManager,
                           ObjectMapper objectMapper, GlobalAsyncTask asyncTask) {
        super(auth, properties, uploadManager, bucketManager, objectMapper);
        this.properties = properties;
        this.asyncTask = asyncTask;
    }

    @Override
    public String uploadImage(MultipartFile multipartFile, String md5) {
        return this.fileUpload(properties.getImageFolder(), multipartFile, md5);
    }

    @Override
    public String uploadVideo(MultipartFile multipartFile, String md5) {
        return this.fileUpload(properties.getVideoFolder(), multipartFile, md5);
    }

    private String fileUpload(String prefixFolder, MultipartFile multipartFile, String md5) {
        String originName = multipartFile.getOriginalFilename();
        ExceptionUtils.isConditionThrowRequest(originName == null || originName.isEmpty(), "文件名称不能为空");
        String fileSuffix = originName.substring(originName.lastIndexOf('.'));
        String newFileName = md5 + fileSuffix;
        String fullFilePath = prefixFolder + makeCurrentDayFolder() + "/" + newFileName;
        try{
            String fileUrl = super.upload(multipartFile.getInputStream(), fullFilePath);
            if (fileUrl != null && !fileUrl.isEmpty()) {
                asyncTask.saveFileUploadLog(md5, originName, newFileName, multipartFile.getSize(), fileUrl);
                return properties.getBucketDomain() + fileUrl;
            }
        }catch (IOException e) {
            logger.error("获取文件上传流失败，错误信息：{}", e.getMessage());
        }
        throw new CustomException(HttpStatus.BAD_REQUEST, "文件上传失败，请重试");
    }

    private String makeCurrentDayFolder() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
}
