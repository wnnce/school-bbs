package com.zeroxn.bbs.web.service;

import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.base.entity.PublicMessage;
import com.zeroxn.bbs.base.entity.UserMessage;
import com.zeroxn.bbs.web.dto.PageQueryDto;
import com.zeroxn.bbs.web.dto.UserPublicMessageDto;

/**
 * @Author: lisang
 * @DateTime: 2023-10-20 17:22:47
 * @Description: 公共消息和用户消息 服务层
 */
public interface MessageService {
    Page<UserPublicMessageDto> pagePublicMessage(PageQueryDto pageDto, Long userId);
    Page<UserMessage> pageUserMessage(PageQueryDto pageDto, Long userId);
    void readPublicMessage(Integer messageId, Long userId);
    void deletePublicMessage(Integer messageId, Long userId);
    void readUserMessage(Integer messageId, Long userId);
    void deleteUserMessage(Integer messageId, Long userId);
}
