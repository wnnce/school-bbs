package com.zeroxn.bbs.task.handler.hot;


import com.zeroxn.bbs.task.dao.TopicCalculate;
import tech.powerjob.worker.log.OmsLogger;

/**
 * @Author: lisang
 * @DateTime: 2023-10-20 12:22:29
 * @Description: 计算其它时间发布的话题热度
 */
public class HotOtherDayHandler extends HotHandler{
    @Override
    public void handleTopicHot(TopicCalculate calculate, OmsLogger logger) {
        if (calculate.getPublishHour() > 120) {

        } else if (nextHandler != null) {
            nextHandler.handleTopicHot(calculate, logger);
        }
    }
}
