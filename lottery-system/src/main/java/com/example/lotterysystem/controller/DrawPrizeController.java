package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.controller.result.WinningRecordResult;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.dto.WinningRecordDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 抽奖模块
 */
@Slf4j
@RestController
@CrossOrigin
public class DrawPrizeController {
    private static final Logger logger = LoggerFactory.getLogger(DrawPrizeController.class);

    @Autowired
    private DrawPrizeService drawPrizeService;

    /*
     *异步抽奖, 控制层直接返回抽奖成功
     * @return
     */
    @RequestMapping("/draw-prize")
    public CommonResult<Boolean> drawPrize(@Validated @RequestBody DrawPrizeParam param) {
        logger.info("drawPrize DrawPrizeParam: " + JacksonUtil.writeValueAsString(param));
        drawPrizeService.drawPrize(param);
        return CommonResult.success(true);
    }


    /**
     * 查询中奖名单
     *
     * @param param
     * @return
     */
    @RequestMapping("/winning-records/show")
    public CommonResult<List<WinningRecordResult>> showWiningRecords(
            @Validated @RequestBody ShowWinningRecordsParam param) {
        logger.info("showWiningRecords showWinningRecordsParam: " + JacksonUtil.writeValueAsString(param));
        //服务层接口返回对象列表
        List<WinningRecordDTO> recordDTOList = drawPrizeService.showWinningRecords(param);
        return CommonResult.success(convertToWinningRecords(recordDTOList));

    }

    /**
     * 服务层的DTOList转换为控制层的返回结果data部分, List<WinningRecordResult>
     *
     * @param recordDTOList
     * @return
     */
    private List<WinningRecordResult> convertToWinningRecords(List<WinningRecordDTO> recordDTOList) {
        //recordDTOList为null或者说空列表
        if (CollectionUtils.isEmpty(recordDTOList)) {
            return Arrays.asList();
        }
        //用stream流转换
        return recordDTOList.stream()
                .map(recordDTO -> {
                    WinningRecordResult result = new WinningRecordResult();
                    result.setWinnerId(recordDTO.getWinnerId());
                    result.setWinnerName(recordDTO.getWinnerName());
                    result.setPrizeName(recordDTO.getPrizeName());
                    result.setPrizeTier(recordDTO.getPrizeTier().getMessage());
                    result.setWinningTime(recordDTO.getWinningTime());
                    return result;
                })
                .toList();
    }
}
