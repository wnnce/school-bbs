package com.zeroxn.bbs.core.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一评论表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_comment")
@Schema(description = "评论")
public class Comment implements Serializable {

    /**
     * ID 主键 自增
     */
    @Id(keyType = KeyType.Auto)
    @Schema(description = "评论Id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;

    /**
     * 评论创建时间
     */
    private Timestamp createTime;

    /**
     * 帖子/话题ID
     */
    private Integer topicId;

    /**
     * 一级评论ID
     */
    private Long fid;

    /**
     * 上级评论ID
     */
    private Long rid;

    /**
     * 发送评论的用户ID
     */
    private Long userId;

    /**
     * 状态 0：正常 1：删除
     */
    private Integer status;

}
