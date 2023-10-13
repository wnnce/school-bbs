package com.zeroxn.bbs.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 18:00:25
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {
    /**
     * 用户在微信中的手机号
     */
    @Schema(description = "用户在微信中的手机号")
    private String phone;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickName;

    /**
     * 用户头像地址
     */
    @Schema(description = "用户头像地址")
    private String avatar;

    /**
     * 用户年龄
     */
    @Schema(description = "用户年龄")
    @DecimalMin(value = "0", message = "年龄不能小于0")
    @DecimalMax(value = "100", message = "年龄过大")
    private Integer age;

    /**
     * 用户性别 0：男 1:女
     */
    @Schema(description = "用户性别")
    @DecimalMin(value = "0", message = "用户性别错误")
    @DecimalMax(value = "1", message = "用户性别错误")
    private Integer gender;

    /**
     * 用户所在地区 省-市，湖北-武汉
     */
    @Schema(description = "用户所在地区")
    private String address;

    /**
     * 用户简介
     */
    @Schema(description = "用户简介")
    private String introduction;
}
