package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件的上传和下载
 *
 * @author li
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    //读取yml配置文件中自己定义的路径reggie.path
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //file是一个临时文件，需要转存指定位置，否则本次请求完成后临时文件会删除
        //参数名一定要叫file不能叫其他的，要跟前端的参数名保持一致
        log.info(file.toString());

        //设置文件的名字
        //第一种方法：获得原始文件名,原先文件叫什么名字，传上来还是叫什么名字
        String originalFilename = file.getOriginalFilename();
        //根据原始文件名来生成后缀，加到UUID生成的文件名的后面
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //第二种方法：使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        //创建一个目录对象
        File dir = new File(basePath);
        //判断当前目录是否存在，若不存在则创建
        if (!dir.exists()) {
            //目录不存在，创建该目录
            dir.mkdirs();
        }

        try {
            //将临时文件转存到指定位置
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //上传成功，返回文件名称
        return R.success(fileName);
    }

    /**
     * 文件下载
     *
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //输出流，通过输出流将文件写回浏览器，在浏览器显示图片
            ServletOutputStream outputStream = response.getOutputStream();

            //设置响应回的是什么类型的文件
            response.setContentType("image/jpg");

            //上传了几张图片就回显几次
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
