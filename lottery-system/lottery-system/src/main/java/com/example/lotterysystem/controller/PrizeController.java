package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.errorcode.ControllerCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.CreatePrizeParam;
import com.example.lotterysystem.controller.param.PageListParam;
import com.example.lotterysystem.controller.result.FindPrizeListResult;
import com.example.lotterysystem.service.PictureService;
import com.example.lotterysystem.service.PrizeService;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.dto.PrizeDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

/**
 * 奖品模块相关的接口
 */
@RestController
@CrossOrigin
public class PrizeController {

    private static final Logger logger = LoggerFactory.getLogger(PrizeController.class);

    @Autowired
    private PrizeService prizeService;
    @Autowired
    private PictureService pictureService;

    @RequestMapping("/pic/upload")
    public String uploadPic(MultipartFile file) {
        return pictureService.savePicture(file);
    }

    /**
     * 创建奖品,
     *
     * @param param    奖品的细节信息
     * @param prizePic 奖品上传的图片
     * @return 返回一个Long 对象
     * @Validated 与 @Valid 的区别
     * 1. 来源与规范
     * @Valid： 是 JSR-303（Bean Validation 1.0）和 JSR-349（Bean Validation 1.1）规范的注解。
     * 属于 Java EE（现 Jakarta EE）的标准注解，位于 javax.validation 包下。
     * @Validated： 是 Spring 框架对 @Valid 的扩展，位于 org.springframework.validation.annotation 包下。
     * 提供了一些 @Valid 没有的特性（如分组校验）。
     * @RequestPart 作用：用于处理 multipart/form-data 请求中的表单字段，通常用于文件上传场景。
     * 参数：
     * value/name：指定表单字段的名称（如 HTML 中 <input name="prizePic">）
     */
    @RequestMapping("/prize/create")
    public CommonResult<Long> createPrize(@Valid @RequestPart("param") CreatePrizeParam param, @RequestPart("prizePic") MultipartFile prizePic) {
        System.out.println(param.toString());
        System.out.println(JacksonUtil.writeValueAsString(param));
        logger.info("createPrize param: {}", JacksonUtil.writeValueAsString(param));
        return CommonResult.success(prizeService.createPrize(param, prizePic));
    }

    /**
     * 查询奖品列表
     *
     * @param param
     * @return
     */
    @RequestMapping("/prize/find-list")
    public CommonResult<FindPrizeListResult> findPrizeList(PageListParam param) {
        logger.info("findPrizeList PageListParam: " + JacksonUtil.writeValueAsString(param));
        PageListDTO<PrizeDTO> prizeListDTO = prizeService.findPrizeList(param);
        return CommonResult.success(converToFindPrizeListResult(prizeListDTO));
    }

    private FindPrizeListResult converToFindPrizeListResult(PageListDTO<PrizeDTO> prizeListDTO) {
        if (null == prizeListDTO) {
            throw new ControllerException(ControllerCodeConstants.FIND_PRIZE_LIST_ERROR);
        }
        FindPrizeListResult result = new FindPrizeListResult();
        //设置属性
        result.setTotal(prizeListDTO.getTotal());
        //由于这两个是不同类型的列表, 每一个元素都要进行转换, 使用stream流
        result.setRecords(prizeListDTO.getRecords().stream().map(prizeDTO -> { //参数, 待转换的对象
            FindPrizeListResult.PrizeInfo prizeInfo = new FindPrizeListResult.PrizeInfo();
            prizeInfo.setPrizeId(prizeDTO.getId());
            prizeInfo.setPrizeName(prizeDTO.getName());
            prizeInfo.setDescription(prizeDTO.getDescription());
            prizeInfo.setPrice(prizeDTO.getPrice());
            prizeInfo.setImageUrl(prizeDTO.getImageUrl());
            return prizeInfo;
        }).collect(Collectors.toList()));
        return result;
    }
}
