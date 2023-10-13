package com.zeroxn.bbs.core.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.BatchStatus;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.zeroxn.bbs.core.config.qiniu.QiniuProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 20:29:46
 * @Description: 七牛云上传服务层
 */
public class QiniuBucketService {
    private static final Logger logger = LoggerFactory.getLogger(QiniuBucketService.class);
    private final Auth auth;
    private final QiniuProperties properties;
    private final UploadManager uploadManager;
    private final BucketManager bucketManager;
    private final ObjectMapper objectMapper;
    public QiniuBucketService(Auth auth, QiniuProperties properties, UploadManager uploadManager, BucketManager bucketManager,
                              ObjectMapper objectMapper) {
        this.auth = auth;
        this.properties = properties;
        this.uploadManager = uploadManager;
        this.bucketManager = bucketManager;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据本地文件路径和文件名称完成文件上传
     * @param filePath 文件的本地路径
     * @param fileName 文件名称
     * @return 上传成功返回文件名称 失败返回null
     */
    public String upload(String filePath, String fileName){
        logger.info("开始上传文件，文件路径：{}，文件名称：{}", filePath, fileName);
        String fileKey = null;
        try{
            String upToken = auth.uploadToken(properties.getBucket());
            Response response = uploadManager.put(filePath, fileName, upToken);
            fileKey = handlerSuccessResponse(response);
        }catch (QiniuException ex){
            Response response = ex.response;
            logger.error("文件上传失败，错误码：{}，错误消息：{}", response.statusCode, response);
            System.out.println(response);
        }catch (JsonProcessingException ex){
            logger.warn("Json序列化错误，错误消息：{}", ex.getMessage());
            fileKey = fileName;
        }
        return fileKey;
    }

    /**
     * 根据文件的输入流和文件名称完成文件上传
     * @param inputStream 文件输入流
     * @param fileName 文件名称
     * @return 上传成功返回文件名称 失败返回null
     */
    public String upload(InputStream inputStream, String fileName) {
        logger.info("开始上传文件，文件名称：{}", fileName);
        String fileKey = null;
        try{
            String upToken = auth.uploadToken(properties.getBucket());
            Response response = uploadManager.put(inputStream, fileName, upToken, null, null);
            fileKey = handlerSuccessResponse(response);
        }catch (QiniuException ex){
            Response response = ex.response;
            logger.error("文件上传失败，错误码：{}，错误消息：{}", response.statusCode, ex.response);
        }catch (JsonProcessingException ex){
            logger.warn("Json序列化错误，错误消息：{}", ex.getMessage());
            fileKey = fileName;
        }finally {
            try {
                inputStream.close();
            }catch (IOException e){
                logger.error("文件流关闭失败，错误消息：{}", e.getMessage());
            }
        }
        return fileKey;
    }

    /**
     * 通过文件名称获取文件的详细信息
     * @param fileName 文件名称
     * @return 返回文件详细信息或null
     */
    public FileInfo getFileInfo(String fileName){
        try{
            return bucketManager.stat(properties.getBucket(), fileName);
        }catch (QiniuException ex){
            logger.error("获取文件信息报错，错误消息：{}，请求错误消息：{}", ex.getMessage(), ex.response.toString());
        }
        return null;
    }

    /**
     * 通过文件名称删除单个文件
     * @param fileName 文件名称
     * @return 删除成功返回true 否则false
     */
    public boolean delete(String fileName) {
        try{
            bucketManager.delete(properties.getBucket(), fileName);
            logger.info("删除文件成功，bucket：{}，filename：{}", properties.getBucket(), fileName);
            return true;
        }catch (QiniuException ex){
            logger.error("删除文件失败，bucket：{}，filename：{}，错误消息：{}", properties.getBucket(), fileName, ex.response.toString());
        }
        return false;
    }

    /**
     * 批量删除文件
     * @param fileNames 文件名称数组
     */
    public void batchDelete(String[] fileNames) {
        try{
            BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
            batchOperations.addDeleteOp(properties.getBucket(), fileNames);
            Response response = bucketManager.batch(batchOperations);
            BatchStatus[] statuses = response.jsonToObject(BatchStatus[].class);
            int successCount = 0;
            for (int i = 0; i < statuses.length; i++) {
                BatchStatus status = statuses[i];
                if(status.code == 200){
                    successCount ++;
                }else {
                    logger.error("{} 删除失败", fileNames[i]);
                }
            }
            logger.info("删除{}个文件，{}个删除成功", fileNames.length, successCount);
        }catch (QiniuException ex){
            logger.error("批量删除文件失败，错误消息：{}", ex.response.toString());
        }
    }
    private String handlerSuccessResponse(Response response) throws QiniuException, JsonProcessingException {
        DefaultPutRet defaultPutRet = objectMapper.readValue(response.bodyString(), DefaultPutRet.class);
        logger.info("文件上传成功，key：{}，hash：{}", defaultPutRet.key, defaultPutRet.hash);
        return defaultPutRet.key;
    }
}
