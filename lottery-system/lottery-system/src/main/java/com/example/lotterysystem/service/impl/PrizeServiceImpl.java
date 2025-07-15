package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.controller.param.CreatePrizeParam;
import com.example.lotterysystem.controller.param.PageListParam;
import com.example.lotterysystem.dao.dataobject.PrizeDO;
import com.example.lotterysystem.dao.mapper.PrizeMapper;
import com.example.lotterysystem.service.PictureService;
import com.example.lotterysystem.service.PrizeService;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.dto.PrizeDTO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrizeServiceImpl implements PrizeService {

    private static final Logger logger = LoggerFactory.getLogger(PrizeServiceImpl.class);

    @Resource
    private PrizeMapper prizeMapper;

    @Autowired
    private PictureService pictureService;

    /**
     * 创建奖品列表的具体实现
     *
     * @param param
     * @param prizePIc
     * @return
     */
    @Override
    public Long createPrize(CreatePrizeParam param, MultipartFile prizePIc) {
        PrizeDO prizeDO = new PrizeDO();
        prizeDO.setName(param.getPrizeName());
        prizeDO.setDescription(param.getDescription());
        prizeDO.setPrice(param.getPrice());
        //保存图片, 获取图片的路径
        String fileName = pictureService.savePicture(prizePIc);
        //然后保存到奖品中
        prizeDO.setImageUrl(fileName);
        //保存奖品到数据库
        prizeMapper.insert(prizeDO);
        return prizeDO.getId();
    }

    /**
     * 查询奖品列表的具体实现
     *
     * @param param
     * @return
     */
    @Override
    public PageListDTO<PrizeDTO> findPrizeList(PageListParam param) {
        //奖品总数
        int count = prizeMapper.count();
        //查询到的奖品
        List<PrizeDO> prizes = prizeMapper.queryPrizesByPage(param.offset(), param.getPageSize());
        //展示到页面上的具体数据, 服务层的
        List<PrizeDTO> records = new ArrayList<>();
        //dao层的奖品列表转换为服务层的页面数据列表
        for (PrizeDO prizeDO : prizes) {
            PrizeDTO prizeDTO = new PrizeDTO();
            prizeDTO.setId(prizeDO.getId());
            prizeDTO.setName(prizeDO.getName());
            prizeDTO.setDescription(prizeDO.getDescription());
            prizeDTO.setPrice(prizeDO.getPrice());
            prizeDTO.setImageUrl(prizeDO.getImageUrl());
            records.add(prizeDTO);
        }
        return new PageListDTO(count, records);
    }
}
