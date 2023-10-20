package com.zeroxn.bbs.web.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.zeroxn.bbs.base.entity.UserMessage;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import com.zeroxn.bbs.web.dto.PageQueryDto;
import com.zeroxn.bbs.web.dto.UserPublicMessageDto;
import com.zeroxn.bbs.web.mapper.PublicMessageMapper;
import com.zeroxn.bbs.web.mapper.UserMessageMapper;
import com.zeroxn.bbs.web.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.zeroxn.bbs.base.entity.table.PublicMessageTableDef.PUBLIC_MESSAGE;
import static com.zeroxn.bbs.base.entity.table.UserMessageTableDef.USER_MESSAGE;

/**
 * @Author: lisang
 * @DateTime: 2023-10-20 17:24:24
 * @Description: 公共消息用户消息 服务层 实现类
 */
@Service
public class MessageServiceImpl implements MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    private final PublicMessageMapper publicMessageMapper;
    private final UserMessageMapper userMessageMapper;

    public MessageServiceImpl(PublicMessageMapper publicMessageMapper, UserMessageMapper userMessageMapper) {
        this.publicMessageMapper = publicMessageMapper;
        this.userMessageMapper = userMessageMapper;
    }

    public int sendUserMessage(UserMessage userMessage) {
        return userMessageMapper.insertSelective(userMessage);
    }
    @Override
    public Page<UserPublicMessageDto> pagePublicMessage(PageQueryDto pageDto, Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("id", "content", "send_time", "read_user_ids @> ARRAY [cast(" + userId + " as bigint)] as is_read")
                .from(PUBLIC_MESSAGE)
                .where("not del_user_ids @> ARRAY [cast(" + userId + " as bigint)]");
        return publicMessageMapper.paginateAs(pageDto.getPage(), pageDto.getSize(), queryWrapper, UserPublicMessageDto.class);
    }

    @Override
    public Page<UserMessage> pageUserMessage(PageQueryDto pageDto, Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(USER_MESSAGE.ID, USER_MESSAGE.USER_ID, USER_MESSAGE.CONTENT, USER_MESSAGE.SEND_TIME, USER_MESSAGE.TYPE,
                        USER_MESSAGE.TOPIC_ID, USER_MESSAGE.COMMENT_ID, USER_MESSAGE.IS_READ)
                .where(USER_MESSAGE.USER_ID.eq(userId));
        return userMessageMapper.paginate(pageDto.getPage(), pageDto.getSize(), queryWrapper);
    }

    @Override
    public void readPublicMessage(Integer messageId, Long userId) {
        checkPublicMessage(messageId);
        int result = publicMessageMapper.readPublicMessage(messageId, userId);
        logger.info("更新公共消息已读用户列表完成，messageId：{}，userId：{}，影响行数：{}", messageId, userId, result);
    }

    @Override
    public void deletePublicMessage(Integer messageId, Long userId) {
        checkPublicMessage(messageId);
        int result = publicMessageMapper.deletePublicMessage(messageId, userId);
        logger.info("更新公共消息删除用户列表完成，messageId：{}，userId：{}，影响行数：{}", messageId, userId, result);
    }

    @Override
    public void readUserMessage(Integer messageId, Long userId) {
        UserMessage userMessage = checkUserMessage(messageId, userId);
        if (!userMessage.getIsRead()) {
            userMessage.setIsRead(true);
            int result = userMessageMapper.update(userMessage);
            logger.info("已读用户消息完成，messageId：{}，userId：{}，影响行数：{}", messageId, userId, result);
        }
    }

    @Override
    public void deleteUserMessage(Integer messageId, Long userId) {
        checkUserMessage(messageId, userId);
        int result = userMessageMapper.deleteById(messageId);
        logger.info("删除用户消息完成，messageId：{}，userId：{}，影响行数：{}", messageId, userId, result);
    }

    private void checkPublicMessage(Integer messageId) {
        long count = publicMessageMapper.selectCountByQuery(new QueryWrapper().where(PUBLIC_MESSAGE.ID.eq(messageId)));
        ExceptionUtils.isConditionThrowRequest(count <= 0, "消息不存在");
    }

    private UserMessage checkUserMessage(Integer messageId, Long userId) {
        UserMessage userMessage = userMessageMapper.selectOneById(messageId);
        ExceptionUtils.isConditionThrowRequest(userMessage == null, "消息不存在");
        ExceptionUtils.isConditionThrowRequest(!userMessage.getUserId().equals(userId), "当前用户无权限操作");
        return userMessage;
    }
}
