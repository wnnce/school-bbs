package com.zeroxn.bbs.task.dao;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * @Author: lisang
 * @DateTime: 2023-10-20 13:17:11
 * @Description:
 */
@Data
@NoArgsConstructor
@ToString
public class TopicCalculate {
    /**
     * 帖子/话题Id
     */
    private Integer id;
    /**
     * 帖子/话题的发布时间距今过了多少小时
     */
    private Integer publishHour;
    /**
     * 帖子/话题的最后一次被评论时间距今过来多少小时
     */
    private Integer lastCommentHour;
    /**
     * 帖子/话题的查看次数 归一化 最大数为100000
     */
    private BigDecimal views;
    /**
     * 帖子/话题的收藏次数 归一化 最大数为10000
     */
    private BigDecimal stars;
    /**
     * 帖子/话题的评论数 归一化 最大数1000
     */
    private BigDecimal comments;
    /**
     * 帖子/话题最终计算出的热度值
     */
    private Double heatLevel = 0.0;

    public TopicCalculate(Integer id, Integer publishHour, Integer lastCommentHour, BigDecimal views, BigDecimal stars, BigDecimal comments) {
        this.id = id;
        this.publishHour = publishHour;
        this.lastCommentHour = lastCommentHour;
        this.views = views;
        this.stars = stars;
        this.comments = comments;
    }

    /**
     * List的排序器，基于热度值排序
     */
    public static Comparator<TopicCalculate> calculateComparator = Comparator.comparingDouble(TopicCalculate::getHeatLevel);
}
