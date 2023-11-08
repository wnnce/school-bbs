package com.zeroxn.bbs.web.service;

import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.web.dto.UserTopicDto;

/**
 * @Author: lisang
 * @DateTime: 2023-11-08 11:09:09
 * @Description: 搜索接口
 */
public interface SearchService {
    Page<UserTopicDto> search(String keyword, Integer page, Integer size);
}
