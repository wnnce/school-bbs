package com.zeroxn.bbs.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 17:34:47
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveOrbit {

    /**
     * 用户Id
     */
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * Gps经纬度坐标
     */
    @Schema(description = "GPS经纬度坐标，中间使用','拼接", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "坐标不能为空")
    private String coordinate;

    /**
     * Gps高度信息
     */
    @Schema(description = "GPS高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "高度不能为空")
    private Float altitude;
}
