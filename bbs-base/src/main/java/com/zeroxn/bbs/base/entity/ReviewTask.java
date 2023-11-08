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
 * @Description: 异常任务类，用于帖子/话题审核时，如发生网络错误，接口调用失败等情况时，对审核异常的帖子/话题进行缓存
 * 而后由任务调度平台开启任务定时审核异常记录，这里的任务结果分别表示文本、图片、视频审核结果。结果true：表示该项审核通过 false表示不通过
 * 如果为空则表示审核异常
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
