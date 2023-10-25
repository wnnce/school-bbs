package com.zeroxn.bbs.base.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @Author: lisang
 * @DateTime: 2023-10-25 12:32:48
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(value = "bbs_review_task")
public class ReviewTask {
    @Id(keyType = KeyType.Auto)
    private Integer id;
    /**
     * 帖子/话题Id
     */
    private Integer topicId;
    /**
     * 任务的创建时间
     */
    @Column(onInsertValue = "current_timestamp")
    private LocalDateTime createTime;
    /**
     * 一阶段任务的执行结果
     */
    private Boolean stage1;
    /**
     * 二阶段任务的执行结果
     */
    private Boolean stage2;
    /**
     * 三阶段任务的执行结果
     */
    private Boolean stage3;
    /**
     * 任务的最后一次执行时间
     */
    @Column(onInsertValue = "current_timestamp", onUpdateValue = "current_timestamp")
    private LocalDateTime executeTime;
    /**
     * 任务的重试次数
     */
    private Integer retryCount;
}
