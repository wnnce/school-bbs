package com.zeroxn.bbs.web.service.impl;

import com.zeroxn.bbs.base.entity.UserAction;
import com.zeroxn.bbs.base.entity.UserOrbit;
import com.zeroxn.bbs.base.constant.QueueConstant;
import com.zeroxn.bbs.web.dto.SaveOrbitDto;
import com.zeroxn.bbs.web.mapper.UserActionMapper;
import com.zeroxn.bbs.web.mapper.UserOrbitMapper;
import com.zeroxn.bbs.web.service.OtherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 17:39:29
 * @Description:
 */
@Service
public class OtherServiceImpl implements OtherService {
    private static final Logger logger = LoggerFactory.getLogger(OtherServiceImpl.class);
    private final UserActionMapper actionMapper;
    private final UserOrbitMapper orbitMapper;
    private final RabbitTemplate rabbitTemplate;

    public OtherServiceImpl(UserActionMapper actionMapper, UserOrbitMapper orbitMapper, RabbitTemplate rabbitTemplate) {
        this.actionMapper = actionMapper;
        this.orbitMapper = orbitMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 保存用户行为信息 保存失败输出error日志
     * @param userAction 用户行为数据
     */
    @Override
    public void saveUserAction(UserAction userAction) {
        int result = actionMapper.insert(userAction);
        if (result <= 0) {
            logger.error("插入用户行为表失败，userId：{}", userAction.getUserId());
        }else {
            rabbitTemplate.convertAndSend(QueueConstant.VIEW_QUEUE, userAction.getTopicId());
            logger.info("用户行文表插入成功，RabbitMQ消息发送成功：topicId:{}", userAction);
        }
    }

    /**
     * 保存用户位置信息，高度通过,与经纬度拼接 保存失败输入error日志
     * @param saveOrbit 用户位置信息
     * @param ipAddress 用户IP地址
     */
    @Override
    public void saveUserOrbit(SaveOrbitDto saveOrbit, String ipAddress) {
        String coordinate = saveOrbit.getCoordinate() + "," + saveOrbit.getAltitude();
        UserOrbit userOrbit = UserOrbit.builder()
                .userId(saveOrbit.getUserId())
                .ipAddress(ipAddress)
                .coordinate(coordinate)
                .build();
        int result = orbitMapper.insert(userOrbit);
        if (result <= 0){
            logger.error("插入用户轨迹表失败，userId：{}", saveOrbit.getUserId());
        }
    }
}
