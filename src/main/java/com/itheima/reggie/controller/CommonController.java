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

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){//file临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        log.info(file.toString());
        String fileName="";
        try {
            //将临时文件转存到指定位置
            String originalFilename = file.getOriginalFilename();
            //防止文件名重复 使用uuid
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
             fileName = UUID.randomUUID().toString()+suffix;
            //创建目录对象
            File dir = new File(basePath);
            //判断目录是否存在
            if(!dir.exists()){
                //目录不存在
                dir.mkdirs();
            }

            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param httpServletResponse
     */
    @GetMapping("/download")
    public void downLoad(String name, HttpServletResponse httpServletResponse){
        try {
            //输入流读取文件内容
            FileInputStream  fileInputStream = new FileInputStream(basePath+name);
            //输出流，通过输出流将文件写回浏览器，在浏览器咱是
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();

            httpServletResponse.setContentType("image/jpeg");
            int len =0;
            byte[] bytes = new byte[1024];
            while((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
