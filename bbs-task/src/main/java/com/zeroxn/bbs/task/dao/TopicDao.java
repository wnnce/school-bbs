package com.zeroxn.bbs.task.dao;

import com.zeroxn.bbs.base.entity.ForumTopic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: lisang
 * @DateTime: 2023-10-20 12:15:44
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicDao extends ForumTopic {

    /**
     * 帖子的评论数
     */
    private Integer commentCount;

    /**
     * 帖子的最后一条评论时间
     */
    private LocalDateTime lastCommentTime;
}
