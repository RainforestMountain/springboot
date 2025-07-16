package com.example.lotterysystem.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传服务
 */

public interface PictureService {
    /**
     * 保存图片
     */
    String savePicture(MultipartFile pic);
}
