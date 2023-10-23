package com.zeroxn.bbs.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.zeroxn.bbs.core.common.BaiduService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-23 14:26:54
 * @Description:
 */
@SpringBootTest
public class TestBaiduCloud {
    @Autowired
    BaiduService baiduService;

    @Test
    public void testBaiduSensitiveTextFilter() {
        String text = "兄弟们，能看片的网站来一个";
        BaiduService.ReviewResult result = baiduService.textReview(text);
        List<String> words = result.data().get(0).hits().get(0).words();
        String word = null;
        if (words.isEmpty()) {
            word = result.data().get(0).msg().replace("存在", "").replace("不合规", "");
        }else {
            word = words.get(0);
        }
        System.out.println(word);
    }

    @Test
    public void textImageReview() {
        String imageUrl = "https://w.wallhaven.cc/full/nk/wallhaven-nkmz71.jpg";
        JsonNode jsonNode = baiduService.imageReview(imageUrl);
        String logId = jsonNode.path("log_id").toString();
        System.out.println(jsonNode);
    }
}
