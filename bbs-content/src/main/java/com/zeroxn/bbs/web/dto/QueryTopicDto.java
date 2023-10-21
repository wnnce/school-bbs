package com.zeroxn.bbs.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author: lisang
 * @DateTime: 2023-10-21 13:40:35
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryTopicDto extends PageQueryDto{
    @NotNull(message = "标签ID不能为空")
    @DecimalMin(value = "0", message = "标签ID参数错误")
    private Integer labelId;
}
