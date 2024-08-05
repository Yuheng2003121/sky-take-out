package com.sky.controller.admin;


import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用相关接口")
@Slf4j
public class CommonController {

    @Autowired
    AliOssUtil aliOssUtil;

    /*
    * 文件上传
    * */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传: {}",file);

        //调用阿里云osS工具类进行文件上传
        try {
            String originalFilename = file.getOriginalFilename();//原始文件名
            String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));//随机文件名+原始文件的扩展名

            String url = aliOssUtil.upload(file.getBytes(), fileName);//文件在网络的请求路径
            return Result.success(url);

        } catch (IOException e) {
            log.error("文件上传失败:{}", e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }


}
