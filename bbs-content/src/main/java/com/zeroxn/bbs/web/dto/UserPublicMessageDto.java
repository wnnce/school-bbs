package com.zeroxn.bbs.web.dto;

import com.zeroxn.bbs.base.entity.PublicMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author: lisang
 * @DateTime: 2023-10-20 17:35:14
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPublicMessageDto extends PublicMessage {

    /**
     * 该通知用户是否已读
     */
    private Boolean isRead;
}
