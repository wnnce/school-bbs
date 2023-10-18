package com.zeroxn.bbs.web.service;

import com.zeroxn.bbs.web.dto.SaveOrbitDto;
import com.zeroxn.bbs.base.entity.UserAction;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 16:42:07
 * @Description: 一些额外操作服务层
 */
public interface OtherService {
    /**
     * 保存用户行为记录
     * @param userAction 用户行为数据
     */
    void saveUserAction(UserAction userAction);

    /**
     * 保存用户位置信息
     * @param saveOrbit 用户位置信息
     * @param ipAddress 用户IP地址
     */
    void saveUserOrbit(SaveOrbitDto saveOrbit, String ipAddress);
}
