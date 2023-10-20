package com.zeroxn.bbs.base.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户消息表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_user_message")
public class UserMessage implements Serializable {

    /**
     * ID 主键 自增
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 消息需要送达的UserId
     */
    private Long userId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息的发送时间
     */
    @Column(onInsertValue = "current_timestamp")
    private LocalDateTime sendTime;
    /**
     * 消息类型 0：话题/帖子回复消息 1：评论回复消息
     */
    private Integer type;

    /**
     * 消息对应的话题/帖子ID
     */
    private Integer topicId;

    /**
     * 消息对应的评论ID,如果是话题/帖子消息则为空
     */
    private Long commentId;

    /**
     * 消息用户是否已读
     */
    private Boolean isRead;

    /**
     * 状态 0：正常 1：删除
     */
    @Column(isLogicDelete = true)
    private Integer status;

    public UserMessage(Long userId, String content, Integer type, Integer topicId, Long commentId) {
        this.userId = userId;
        this.content = content;
        this.type = type;
        this.topicId = topicId;
        this.commentId = commentId;
    }
}
