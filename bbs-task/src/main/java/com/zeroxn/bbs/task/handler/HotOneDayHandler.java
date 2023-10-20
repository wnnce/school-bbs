package com.zeroxn.bbs.task.handler;


import com.zeroxn.bbs.task.dao.TopicCalculate;
import tech.powerjob.worker.log.OmsLogger;

/**
 * @Author: lisang
 * @DateTime: 2023-10-20 12:02:45
 * @Description: 计算一天内发布的话题热度
 */
public class HotOneDayHandler extends HotHandler {

    @Override
    public void handleTopicHot(TopicCalculate calculate, OmsLogger logger) {
        if (calculate.getPublishHour() <= 24) {
            logger.debug("责任链匹配成功，topicId：{}，距今发布：{}小时", calculate.getId(), calculate.getPublishHour());


        }else if (this.nextHandler != null) {
            nextHandler.handleTopicHot(calculate, logger);
        }
    }
}
