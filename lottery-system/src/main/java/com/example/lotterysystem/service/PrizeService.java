package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.CreatePrizeParam;
import com.example.lotterysystem.controller.param.PageListParam;
import com.example.lotterysystem.dao.dataobject.PrizeDO;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.dto.PrizeDTO;
import org.springframework.web.multipart.MultipartFile;


public interface PrizeService {

    /**
     * 创建奖品
     *
     * @param param
     * @param prizePIc
     * @return
     */
    Long createPrize(CreatePrizeParam param, MultipartFile prizePIc);

    PageListDTO<PrizeDTO> findPrizeList(PageListParam param);
}
