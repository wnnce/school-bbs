package com.zeroxn.bbs.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: lisang
 * @DateTime: 2023-11-08 13:23:56
 * @Description: 搜索接口查询条件封装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDto {
    private String keyword;
}
