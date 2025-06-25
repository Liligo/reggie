package com.liligo.reggie.controller;

import com.liligo.reggie.common.Result;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

        // 使用uuid重新生成文件名，防止文件名称重复造成文件覆盖
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
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
     * 文件下载接口
     *
     * @param name     the name of the file to be downloaded
     * @param response the HttpServletResponse to write the file to
     * @throws IOException if an I/O error occurs during file download
     */
    @GetMapping("/download")
    public void download(@RequestParam("name") String name, HttpServletResponse response) throws IOException {
        // 组合文件路径
        Path filePath = Paths.get(basePath).resolve(name).normalize();
        if (!filePath.startsWith(basePath)) {
            throw new FileNotFoundException("文件路径不合法");
        }

        // 设置响应内容类型为图片, 根据文件后缀自动识别
        String contentType = Files.probeContentType(filePath);
        response.setContentType(contentType != null ? contentType : "application/octet-stream");

        // 输入流，读取文件内容
        FileInputStream fileInputStream = new FileInputStream(filePath.toFile());

        // 输出流，将文件写回浏览器
        ServletOutputStream outputStream = response.getOutputStream();

        // 读取文件并写入输出流
        int len = 0;
        byte[] buffer = new byte[1024];
        while ((len = fileInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
            outputStream.flush();
        }

        // 关闭流
        outputStream.close();
        fileInputStream.close();
    }
}
