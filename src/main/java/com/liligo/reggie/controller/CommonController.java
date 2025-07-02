package com.liligo.reggie.controller;

import com.liligo.reggie.common.Result;
import com.liligo.reggie.service.ImageService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    // 使用配置文件的路径
    @Value("${reggie.path}")
    private String basePath;

    @Autowired
    private ImageService imageService;

    /**
     * 文件上传接口
     *
     * @param file the file to be uploaded
     * @return a Result object containing the file name
     * @throws IOException if an I/O error occurs during file upload
     */
    @PostMapping("/upload")
    public Result<String> upload( MultipartFile file) throws IOException {
        // file是一个临时文件，需要进行转存，否则本次请求完成后临时文件会删除
        log.info("文件上传：{}", file.getOriginalFilename());

        // 获取原始文件名和后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID() + suffix;

        // 确保目录存在
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs(); // 创建目录
        }

        file.transferTo(new File(basePath + fileName));

        return Result.success(fileName);
    }

    /**
     * 文件下载（新版，支持二级缓存）
     * @param name 文件名
     * @param response HttpServletResponse
     * @throws IOException 如果读取文件时发生I/O错误
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        // 从缓存或文件系统获取图片字节
        byte[] imageBytes = imageService.getImageBytes(name);

        // 如果 imageBytes 为 null (文件不存在或路径非法)，直接返回404
        if (imageBytes == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 动态设置响应头 Content-Type
        Path path = Paths.get(basePath, name);
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream"; // 如果无法确定类型，则使用通用二进制流
        }
        response.setContentType(contentType);

        // 将字节写入响应流
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(imageBytes);
            outputStream.flush();
        }
    }
}
