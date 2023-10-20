package com.zeroxn.bbs.test;

import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.base.entity.UserMessage;
import com.zeroxn.bbs.web.dto.PageQueryDto;
import com.zeroxn.bbs.web.dto.UserPublicMessageDto;
import com.zeroxn.bbs.web.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: lisang
 * @DateTime: 2023-10-20 18:25:28
 * @Description:
 */
@SpringBootTest
public class TestMessageService {
    @Autowired
    MessageService messageService;

    @Test
    public void testUserPublicMessageDto() {
        Page<UserPublicMessageDto> userPublicMessageDtoPage = messageService.pagePublicMessage(new PageQueryDto(1, 5), 1L);
        System.out.println(userPublicMessageDtoPage);
    }
    @Test
    public void testUserMessagePage() {
        Page<UserMessage> userMessagePage = messageService.pageUserMessage(new PageQueryDto(1, 5), 1L);
        System.out.println(userMessagePage);
    }
}
