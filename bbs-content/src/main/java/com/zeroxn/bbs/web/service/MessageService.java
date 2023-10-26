package com.zeroxn.bbs.web.service;

import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.base.entity.UserMessage;
import com.zeroxn.bbs.web.dto.PageQueryDto;
import com.zeroxn.bbs.web.dto.UserPublicMessageDto;

/**
 * @Author: lisang
 * @DateTime: 2023-10-20 17:22:47
 * @Description: 公共消息和用户消息 服务层
 */
public interface MessageService {
    /**
     * 发送用户消息
     * @param userMessage 封装参数的对象
     * @return 返回数据库的影响行数
     */
    int sendUserMessage(UserMessage userMessage);

    /**
     * 获取当前用户的公共消息信息
     * @param pageDto 分页查询参数
     * @param userId 当前用户的Id
     * @return 返回公共消息信息
     */
    Page<UserPublicMessageDto> pagePublicMessage(PageQueryDto pageDto, Long userId);

    /**
     * 获取当前用户的用户消息信息
     * @param pageDto 分页查询参数
     * @param userId 当前用户的id
     * @return 返回用户消息信息
     */
    Page<UserMessage> pageUserMessage(PageQueryDto pageDto, Long userId);

    /**
     * 用户已读某条公共消息
     * @param messageId 公共消息Id
     * @param userId 用户id
     */
    void readPublicMessage(Integer messageId, Long userId);

    /**
     * 用户删除某条公共消息
     * @param messageId 消息Id
     * @param userId 用户id
     */
    void deletePublicMessage(Integer messageId, Long userId);

    /**
     * 用户已读私人用户消息
     * @param messageId 私人消息Id
     * @param userId 用户id
     */
    void readUserMessage(Integer messageId, Long userId);

    /**
     * 用户删除私人用户消息
     * @param messageId 私人消息id
     * @param userId 用户id
     */
    void deleteUserMessage(Integer messageId, Long userId);
}
