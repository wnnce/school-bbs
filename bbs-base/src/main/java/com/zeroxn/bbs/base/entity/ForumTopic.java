package com.zeroxn.bbs.base.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.zeroxn.bbs.base.mybatis.handlers.ArrayTypeHandler;
import com.zeroxn.bbs.base.validation.ValidationGroups.SavePostValidation;
import com.zeroxn.bbs.base.validation.ValidationGroups.SaveTopicValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;
import org.hibernate.validator.constraints.Length;

/**
 * 论坛帖子/话题信息表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_forum_topic")
public class ForumTopic implements Serializable {

    /**
     * 帖子/话题ID 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 帖子/话题标题
     */
    @NotBlank(message = "标题不能为空", groups = { SavePostValidation.class, SaveTopicValidation.class })
    @Length(max = 200, message = "标题长度超出限制", groups = { SaveTopicValidation.class, SavePostValidation.class })
    @Schema(description = "话题或帖子的标题", minLength = 4, maxLength = 200, requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * 帖子/话题内容
     */
    @NotBlank(message = "内容不能为空", groups = { SavePostValidation.class, SaveTopicValidation.class })
    @Schema(description = "话题帖子内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    /**
     * 帖子/话题关键字
     */
    private String contentKey;

    /**
     * 图片连接数组
     */
    @Schema(description = "话题帖子的图片链接数组", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Column(jdbcType = JdbcType.ARRAY, typeHandler = ArrayTypeHandler.class)
    private String[] imageUrls;

    /**
     * 视频链接
     */
    @Schema(description = "话题帖子的视频链接", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String videoUrl;

    /**
     * 类型 0:帖子 1：话题
     */
    private Integer type;

    /**
     * 帖子标签 0：求助信息 1：讨论交流 2：学习资料 3：二手交易 4：失物招领 话题则空
     */
    @NotNull(message = "帖子类型不能为空", groups = { SavePostValidation.class })
    @DecimalMin(value = "0", message = "帖子类型错误", groups = { SavePostValidation.class })
    @DecimalMax(value = "4", message = "帖子类型错误", groups = { SavePostValidation.class })
    private Integer flag;

    /**
     * 话题的标签列表，帖子则为空
     */
    @NotNull(message = "话题标签不能为空", groups = { SaveTopicValidation.class })
    @Column(jdbcType = JdbcType.ARRAY, typeHandler = ArrayTypeHandler.class)
    private Integer[] labelIds;

    /**
     * 是否热门话题
     */
    private Boolean isHot;

    /**
     * 查看次数
     */
    private Integer viewCount;

    /**
     * 收藏次数
     */
    private Integer starCount;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "current_timestamp")
    private LocalDateTime createTime;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 状态 0：正常 1：已删除
     */
    @Column(isLogicDelete = true)
    private Integer status;

}
