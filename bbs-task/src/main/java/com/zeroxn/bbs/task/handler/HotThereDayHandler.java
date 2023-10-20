package com.zeroxn.bbs.task.handler;


import com.zeroxn.bbs.task.dao.TopicCalculate;
import tech.powerjob.worker.log.OmsLogger;

/**
 * @Author: lisang
 * @DateTime: 2023-10-20 12:03:53
 * @Description: 计算三天内发布的话题热度
 */
public class HotThereDayHandler extends HotHandler {
    @Override
    public void handleTopicHot(TopicCalculate calculate, OmsLogger logger) {
        if (calculate.getPublishHour() > 24 && calculate.getPublishHour() >= 72) {

        } else if (this.nextHandler != null) {
            nextHandler.handleTopicHot(calculate, logger);
        }
    }
}
