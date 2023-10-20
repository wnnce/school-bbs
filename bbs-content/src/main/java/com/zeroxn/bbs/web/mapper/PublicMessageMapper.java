package com.zeroxn.bbs.web.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zeroxn.bbs.base.entity.PublicMessage;
import com.zeroxn.bbs.web.dto.UserPublicMessageDto;
import io.swagger.v3.oas.annotations.links.Link;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公共消息表 映射层。
 *
 * @author lisang
 * @since 2023-10-12
 */
public interface PublicMessageMapper extends BaseMapper<PublicMessage> {
    int readPublicMessage(@Param("messageId") Integer messageId, @Param("userId") Long userId);
    int deletePublicMessage(@Param("messageId") Integer messageId, @Param("userId") Long userId);
}
