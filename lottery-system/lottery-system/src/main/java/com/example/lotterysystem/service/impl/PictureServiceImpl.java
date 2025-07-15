package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.service.PictureService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class PictureServiceImpl implements PictureService {

    /**
     * 获取配置项的属性值, 然后把值赋给picLocalPath
     */
    @Value("${pic.local-path}")
    private String picLocalPath;

    /**
     * 图片上传的具体实现
     * 会使用io流]
     * MultipartFile是spring框架下的文件类
     * MultipartFile 是 Spring Framework 提供的一个接口，用于处理 HTTP 多部分请求（即文件上传）。
     * 它是 Spring MVC 框架中处理文件上传的核心组件，
     * 提供了访问上传文件内容、元数据（如文件名、类型）的方法。
     *
     * @param pic
     * @return
     */
    @Override
    public String savePicture(MultipartFile pic) {
        File dir = new File(picLocalPath);
        if (!dir.exists()) {
            //不存在的话, 那就创建多级目录
            dir.mkdirs();
        }
        //获取文件名
        String fileName = pic.getOriginalFilename();

        //当执行到 assert fileName != null; 时，Java 会检查 fileName 是否为 null：
        //如果 fileName 确实不为 null，断言通过，程序继续正常执行。
        //如果 fileName 是 null，断言失败，抛出 AssertionError 异常，终止程序运行。
        assert fileName != null;

        //获取文件后缀名, 使用字符串的截取, 找到最后出现的点
        String suffixName = fileName.substring(fileName.lastIndexOf("."));

        //可以添加判断语句, 规定特定格式的图片才能上传, 否则拒绝保存

        //生成随机数防止图片重名
        fileName = UUID.randomUUID() + suffixName;
        try {
            pic.transferTo(new File(picLocalPath + "/" + fileName));
        } catch (IOException e) {
            //图片上传出现异常
            throw new ServiceException(ServiceErrorCodeConstants.UPLOAD_PIC_ERROR);
        }
        return fileName;
    }
}
