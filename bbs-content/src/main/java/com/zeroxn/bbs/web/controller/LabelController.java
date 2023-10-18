package com.zeroxn.bbs.web.controller;

import com.zeroxn.bbs.base.entity.TopicLabel;
import com.zeroxn.bbs.web.dto.Result;
import com.zeroxn.bbs.web.service.LabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-18 15:34:56
 * @Description: 话题标签控制层
 */
@RestController
@RequestMapping("/label")
@Tag(name = "话题标签接口")
public class LabelController {

    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping("/list")
    @Operation(description = "获取所有标签接口")
    public Result<List<TopicLabel>> listAllLabel() {
        List<TopicLabel> labelList = labelService.listTopicLabel();
        return Result.success(labelList);
    }
}
