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
    private Integer id;
    private Integer publishHour;
    private Integer lastCommentHour;
    private BigDecimal views;
    private BigDecimal stars;
    private BigDecimal comments;
    private Double heatLevel = 0.0;

    public TopicCalculate(Integer id, Integer publishHour, Integer lastCommentHour, BigDecimal views, BigDecimal stars, BigDecimal comments) {
        this.id = id;
        this.publishHour = publishHour;
        this.lastCommentHour = lastCommentHour;
        this.views = views;
        this.stars = stars;
        this.comments = comments;
    }

    public static Comparator<TopicCalculate> calculateComparator = Comparator.comparingDouble(TopicCalculate::getHeatLevel);
}
