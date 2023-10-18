package com.zeroxn.bbs.base.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
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
     * 消息类型 0：话题/帖子回复消息 1：评论回复消息
     */
    private Integer type;

    /**
     * 点击消息需要跳转的内容ID
     */
    private Long sourceId;

    /**
     * 消息用户是否已读
     */
    private Boolean isRead;

    /**
     * 状态 0：正常 1：删除
     */
    private Integer status;

}
