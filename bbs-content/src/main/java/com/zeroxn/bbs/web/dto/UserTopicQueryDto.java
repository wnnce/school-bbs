package com.zeroxn.bbs.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author: lisang
 * @DateTime: 2023-10-18 16:15:55
 * @Description: 查询用户发布、收藏的帖子/话题参数类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTopicQueryDto extends PageQueryDto{

    /**
     * 查询帖子还是话题 null：查询所有 0：查询帖子 1：查询话题
     */
    @DecimalMin(value = "0", message = "查询参数错误")
    @DecimalMax(value = "1", message = "查询参数错误")
    private Integer type;
}
