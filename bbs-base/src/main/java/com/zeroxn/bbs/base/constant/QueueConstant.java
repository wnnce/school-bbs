package com.zeroxn.bbs.base.constant;

/**
 * @Author: lisang
 * @DateTime: 2023-10-28 17:44:09
 * @Description: 论坛RabbitMQ Queue名称常量类
 */
public final class QueueConstant {
    /**
     * 帖子/话题发布后发布消息的Queue
     */
    public static final String TOPIC_QUEUE = "bbs.topic";
    /**
     * 用户点击某个帖子/话题后发布消息的Queue
     */
    public static final String VIEW_QUEUE = "bbs.view";
    /**
     * 帖子/话题删除后发布消息的Queue
     */
    public static final String DELETE_QUEUE = "bbs.delete";
    /**
     * 同步帖子/话题查看数到Solr索引的Queue
     */
    public static final String INDEX_VIEW_COUNT_QUEUE = "bbs.index.view";
    /**
     * 同步帖子/话题收藏数到Solr索引的Queue
     */
    public static final String INDEX_STAR_COUNT_QUEUE = "bbs.index.star";
    /**
     * 同步帖子/话题评论数到Solr索引的Queue
     */
    public static final String INDEX_COMMENT_COUNT_QUEUE = "bbs.index.comment";
    /**
     * 删除索引库资源的Queue
     */
    public static final String INDEX_DELETE_QUEUE = "bbs.index.delete";
}
