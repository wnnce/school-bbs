package com.zeroxn.bbs.core.solr;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: lisang
 * @DateTime: 2023-11-07 20:30:40
 * @Description: solr操作索引库记录
 */
public final class SolrRecordOperate {
    private static final Logger logger = LoggerFactory.getLogger(SolrRecordOperate.class);

    public final HttpSolrClient solrClient;

    public SolrRecordOperate(HttpSolrClient solrClient) {
        this.solrClient = solrClient;
    }

    /**
     * 通过实体类添加Solr索引，需要为实体类添加@Document注解
     * @param record 需要添加的索引对象
     */
    public void saveRecord(Object record) {
        final String indexName = this.getRecordClassIndexName(record.getClass());
        this.sendSaveRecordRequest(indexName, record);
    }

    /**
     * 通过索引库名称和实体对象添加solr索引
     * @param indexName 索引库名称
     * @param record 实体对象
     */
    public void saveRecord(String indexName, Object record) {
        this.sendSaveRecordRequest(indexName, record);
    }

    /**
     * 通过实体类删除Solr索引，需要为实体类添加@Document注解和为ID字段添加@Id注解
     * @param record 实体类对象，Id字段必须赋值
     */
    public void deleteRecord(Object record) {
        final String indexName = this.getRecordClassIndexName(record.getClass());
        final String id = this.getRecordClassId(record);
        this.sendDeleteRecordRequest(indexName, id);
    }

    /**
     * 通过索引库名称和记录Id删除Solr索引
     * @param indexName 索引库名称
     * @param id 记录Id
     */
    public void deleteRecord(String indexName, String id) {
        this.sendDeleteRecordRequest(indexName, id);
    }

    private void sendSaveRecordRequest(String indexName, Object record) {
        try {
            final UpdateResponse updateResponse = solrClient.addBean(indexName, record);
            System.out.println(updateResponse);
            solrClient.commit(indexName);
        } catch (IOException | SolrServerException ex) {
            logger.error("solr新增记录到索引库异常，错误信息：{}，索引库名称：{}", ex.getMessage(), indexName);
        }
    }

    private void sendDeleteRecordRequest(String indexName, String id) {
        try {
            final UpdateResponse updateResponse = solrClient.deleteById(indexName, id);
            System.out.println(updateResponse);
            solrClient.commit(indexName);
        }catch (IOException | SolrServerException ex) {
            logger.error("solr删除索引库记录异常，错误信息：{}，索引库名称：{}，ID：{}", ex.getMessage(), indexName, id);
        }
    }

    private String getRecordClassIndexName(Class<?> clazz) {
        final Document annotation = clazz.getAnnotation(Document.class);
        Assert.notNull(annotation, "实体类型的Document注解不能为空");
        return annotation.indexName();
    }

    private String getRecordClassId(Object record) {
        final Field[] declaredFields = record.getClass().getDeclaredFields();
        for (final Field field : declaredFields) {
            final Id annotation = field.getAnnotation(Id.class);
            if (annotation != null) {
                field.setAccessible(true);
                try {
                    return field.get(record).toString();
                }catch (IllegalAccessException ex) {
                    logger.error("反射获取记录的ID值异常，错误信息：{}", ex.getMessage());
                }
            }
        }
        return null;
    }
}
