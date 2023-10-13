package com.zeroxn.bbs.web.controller;

import com.zeroxn.bbs.web.dto.Result;
import com.zeroxn.bbs.web.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 20:07:27
 * @Description: 文件上传控制层
 */
@Tag(name = "文件上传接口")
@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }
    @PostMapping("/images")
    @Operation(description = "图片上传接口")
    public Result<String> uploadImage(@RequestPart("file") MultipartFile multipartFile, @RequestParam("md5") String md5) {
        String imageUrl = fileService.uploadImage(multipartFile, md5);
        return Result.success(imageUrl);
    }
    @PostMapping("/video")
    @Operation(description = "视频上传接口")
    public Result<String> uploadVideo(@RequestPart("file") MultipartFile multipartFile, @RequestParam("md5") String md5) {
        String videoUrl = fileService.uploadVideo(multipartFile, md5);
        return Result.success(videoUrl);
    }
}
