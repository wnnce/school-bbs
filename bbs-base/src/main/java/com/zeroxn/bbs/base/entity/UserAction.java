package com.zeroxn.bbs.base.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.zeroxn.bbs.base.validation.ValidationGroups.SaveValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_user_action")
public class UserAction implements Serializable {

    /**
     * ID 主键 自增
     */
    @Id(keyType = KeyType.Auto)
    @Schema(description = "ID 自动生成", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 浏览的帖子/话题ID
     */
    @NotNull(message = "帖子/话题ID不能为空")
    @Schema(description = "话题/帖子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer topicId;

    /**
     * 记录创建时间
     */
    @Column(onInsertValue = "current_timestamp")
    @Schema(description = "记录创建时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime createTime;

    /**
     * 浏览时长
     */
    @Schema(description = "用户浏览时长", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "浏览时长不能为空")
    @Size(min = 0, message = "浏览时长不能小于0", groups = { SaveValidation.class })
    private Integer stayTime;

}
