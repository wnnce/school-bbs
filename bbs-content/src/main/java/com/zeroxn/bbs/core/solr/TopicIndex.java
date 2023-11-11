package com.zeroxn.bbs.core.solr;

import lombok.*;
import org.apache.solr.client.solrj.beans.Field;

/**
 * @Author: lisang
 * @DateTime: 2023-11-07 20:16:53
 * @Description: 帖子/话题索引对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
// Solr索引库名称
@Document(indexName = "collection1")
public class TopicIndex {
    /**
     * 帖子/话题ID
     */
    @Id
    @Field
    private Integer id;
    /**
     * 帖子标题
     */
    @Field("bbs_title")
    private String title;
    /**
     * 帖子内容
     */
    @Field("bbs_content")
    private String content;
    /**
     * 帖子内容关键字
     */
    @Field("bbs_content_key")
    private String contentKey;
    /**
     * 帖子/话题类型
     */
    @Field("bbs_type")
    private Integer type;
    /**
     * 帖子分区
     */
    @Field("bbs_flag")
    private Integer flag;

    @Field("bbs_user_id")
    private Long userId;
}

