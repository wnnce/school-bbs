package com.zeroxn.bbs.base.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    private Long id;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "评论用户不能为空")
    private String content;

    /**
     * 评论创建时间
     */
    @Column(onInsertValue = "current_timestamp")
    private LocalDateTime createTime;

    /**
     * 帖子/话题ID
     */
    @Schema(description = "帖子/话题ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "帖子/话题ID不能为空")
    private Integer topicId;

    /**
     * 一级评论ID
     */
    @Schema(description = "一级评论的Id，如果评论是一级评论那么为0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "评论参数错误")
    private Long fid;

    /**
     * 上级评论ID
     */
    @Schema(description = "上级评论Id，如果是一级评论那么为0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "评论参数错误")
    private Long rid;

    /**
     * 发送评论的用户ID
     */
    private Long userId;

    /**
     * 状态 0：正常 1：删除
     */
    @Column(isLogicDelete = true)
    private Integer status;

}
