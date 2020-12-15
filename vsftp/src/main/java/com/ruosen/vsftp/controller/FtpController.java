package com.ruosen.vsftp.controller;


import com.ruosen.vsftp.utils.FtpUtil;
import com.ruosen.vsftp.utils.IDUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@RestController
@RequestMapping("ftp")
public class FtpController {

    @Value("${ftp.basePath}")
    private String basePath;

    @Autowired
    private FtpUtil ftpUtil;

    @PostMapping("upload")
    public String upload(@RequestParam("pic") MultipartFile uploadFile) throws IOException {

        //1、给上传的图片生成新的文件名
        //1.1获取原始文件名
        String oldName = uploadFile.getOriginalFilename();
        if (StringUtils.isBlank(oldName)) {
            throw new RuntimeException("文件名不能为空");
        }
        //1.2使用IDUtils工具类生成新的文件名，新文件名 = newName + 文件后缀
        String newName = IDUtil.genImageName();
        String type = oldName.substring(oldName.lastIndexOf("."));
        oldName = oldName.substring(0, oldName.lastIndexOf("."));
        newName = newName +"-"+ oldName + type;
        //1.3生成文件在服务器端存储的子目录
        String filePath = "/" + DateFormatUtils.format(new Date(), "yyyy-MM-dd");

        //3、把图片上传到图片服务器
        //3.1获取上传的io流
        InputStream input = uploadFile.getInputStream();

        //3.2调用FtpUtil工具类进行上传
        return ftpUtil.uploadFile(basePath, filePath, newName, input);
    }
}
