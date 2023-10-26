package com.zeroxn.bbs.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

/**
 * @Author: lisang
 * @DateTime: 2023-10-17 12:50:52
 * @Description: 查询帖子列表参数Dto类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QueryPostDto extends PageQueryDto{
    /**
     * 获取的帖子类型，0：其他 1：失物招领 2：二手交易 3：校园求助 4：学习资源
     */
    @Schema(description = "获取的帖子类型，0：其他 1：失物招领 2：二手交易 3：校园求助 4：学习资源", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DecimalMin(value = "0", message = "帖子类型错误")
    @DecimalMax(value = "4", message = "帖子类型错误")
    private Integer flag;

    /**
     * 其余的查询参数 0：按查看次数排序  1：按评论数排序
     */
    @Schema(description = "额外查询参数，为空则按创建时间降序排序，0：查看次数排序，1：评论数排序", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DecimalMin(value = "0", message = "查询参数错误")
    @DecimalMax(value = "1", message = "查询参数错误")
    private Integer condition;
}
