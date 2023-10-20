package com.zeroxn.bbs.task.test;

import com.hankcs.hanlp.HanLP;
import com.zeroxn.bbs.task.analytics.TextAnalytics;
import com.zeroxn.bbs.task.dao.TopicCalculate;
import com.zeroxn.bbs.task.dao.TopicDao;
import com.zeroxn.bbs.task.handler.HotHandler;
import com.zeroxn.bbs.task.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-19 17:00:16
 * @Description:
 */
@SpringBootTest
public class XunfeiTest {

    @Autowired
    TextAnalytics analytics;

    @Autowired
    TopicService topicService;

    @Autowired
    HotHandler hotHandler;

    @Test
    public void testXunfeiTextAnalytics() {
        String doc = "STOMP 是一种基于文本的消息传递协议，可用于在客户端和服务器之间进行异步通信。建立在基础的 WebSocket 协议上，并提供了一种简单、灵活的方式来实现消息传递。Spring对STOMP协议进行了封装，提供了注解驱动、消息代理配置、广播和点对点消息、消息转换。";
        List<String> keywordList = analytics.keywordsExtraction(doc, 5);
        keywordList.forEach(System.out::println);
    }

    @Test
    public void testHandlpTextAnalytics () {
        String doc = "STOMP 是一种基于文本的消息传递协议，可用于在客户端和服务器之间进行异步通信。建立在基础的 WebSocket 协议上，并提供了一种简单、灵活的方式来实现消息传递。Spring对STOMP协议进行了封装，提供了注解驱动、消息代理配置、广播和点对点消息、消息转换。";
        List<String> keywordList = HanLP.extractKeyword(doc, 5);
        keywordList.forEach(System.out::println);
    }

    @Test
    public void testTopic() {
        List<TopicDao> topicDaoList = topicService.listAllTopic();
        List<TopicCalculate> topicCalculateList = topicDaoList.stream().map(topic -> {
            LocalDateTime now = LocalDateTime.now();
            int publishHour = (int) Math.ceil(ChronoUnit.HOURS.between(topic.getCreateTime(), now));
            int lastCommentHour = 0;
            if (topic.getLastCommentTime() != null) {
                lastCommentHour = (int) Math.ceil(ChronoUnit.HOURS.between(topic.getLastCommentTime(), now));
            }
            BigDecimal views = new BigDecimal(0), stars = new BigDecimal(0), comments = new BigDecimal(0);
            if (topic.getViewCount() > 0) {
                views = new BigDecimal(topic.getViewCount()).divide(BigDecimal.valueOf(100000), 5, RoundingMode.HALF_UP);
            }
            if (topic.getStarCount() > 0) {
                stars = new BigDecimal(topic.getStarCount()).divide(BigDecimal.valueOf(10000), 4, RoundingMode.HALF_UP);
            }
            if (topic.getCommentCount() > 0) {
                comments = new BigDecimal(topic.getCommentCount()).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            }
            return new TopicCalculate(topic.getId(), publishHour, lastCommentHour, views, stars, comments);
        }).toList();
        topicCalculateList.forEach(calculate -> {
            BigDecimal publishTimeWeight = new BigDecimal(calculate.getPublishHour())
                    .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(-0.4));
            BigDecimal commentTimeWeight = new BigDecimal(calculate.getLastCommentHour())
                    .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(0.2));
            BigDecimal viewsWeight = calculate.getViews().multiply(BigDecimal.valueOf(0.2));
            BigDecimal starsWeight = calculate.getStars().multiply(BigDecimal.valueOf(0.3));
            BigDecimal commentsWeight = calculate.getComments().multiply(BigDecimal.valueOf(0.5));
            double heatLevel = publishTimeWeight.add(commentTimeWeight).add(viewsWeight).add(starsWeight).add(commentsWeight).doubleValue();
            calculate.setHeatLevel(heatLevel);
        });
        System.out.println(topicCalculateList);
    }
}
