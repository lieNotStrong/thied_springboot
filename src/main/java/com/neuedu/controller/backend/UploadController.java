package com.neuedu.controller.backend;


import com.neuedu.common.Constant;
import com.neuedu.common.ServerResponse;
import com.neuedu.vo.ImageVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/manage")
public class UploadController {

    @Value("${thied_springboot.imageHost}")
    private String imageHost;


    @GetMapping(value = "/upload")
    public String upload(){
        return "upload";
    }


    @PostMapping(value = "/upload")
    @ResponseBody
    public ServerResponse upload(@RequestParam("uploadfile") MultipartFile uploadfile){
        if (uploadfile==null||uploadfile.getOriginalFilename().equals("")){
            return ServerResponse.serverResponseByError(Constant.ERROR,"图片必须上传");
        }
        //获取上传的图片名称
        String oldFileName = uploadfile.getOriginalFilename();

        //获取文件扩展名
        String extendName = oldFileName.substring(oldFileName.lastIndexOf('.'));

        //生成新的文件名
        String newFileName = UUID.randomUUID().toString()+extendName;


        File file = new File("e:/upload");
        if (!file.exists()){
            file.mkdirs();
        }

        File file1 = new File(file, newFileName);
        try {
            uploadfile.transferTo(file1);
            ImageVO imageVO = new ImageVO(newFileName,imageHost+newFileName);
            return ServerResponse.serverResponseBySuccess(imageVO);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ServerResponse.serverResponseByError();
    }
}
