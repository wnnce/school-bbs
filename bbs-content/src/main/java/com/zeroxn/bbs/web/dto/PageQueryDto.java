package com.zeroxn.bbs.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author: lisang
 * @DateTime: 2023-10-17 12:45:44
 * @Description: 分页查询参数Dto类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PageQueryDto {
    /**
     * 页码
     */
    @Schema(description = "查询的页码", defaultValue = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DecimalMin(value = "1", message = "页码不能小于0")
    private Integer page = 1;
    /**
     * 每页记录数
     */
    @Schema(description = "分页查询每页返回的记录数", defaultValue = "5", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DecimalMin(value = "1", message = "记录数不能小于0")
    @DecimalMax(value = "100", message = "记录数过大")
    private Integer size = 5;
}
