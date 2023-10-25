package com.zeroxn.bbs.task.handler.hot;


import com.zeroxn.bbs.task.dao.TopicCalculate;
import tech.powerjob.worker.log.OmsLogger;

/**
 * @Author: lisang
 * @DateTime: 2023-10-20 11:59:54
 * @Description: 热门话题算法责任链抽象类 暂时弃用
 */
public abstract class HotHandler {
    protected HotHandler nextHandler;
    public void setNextHandler(HotHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public abstract void handleTopicHot(TopicCalculate calculate, OmsLogger logger);
}
