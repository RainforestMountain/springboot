package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.errorcode.ControllerCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.PageListParam;
import com.example.lotterysystem.controller.result.CreateActivityResult;
import com.example.lotterysystem.controller.result.FindActivityListResult;
import com.example.lotterysystem.controller.result.GetActivityDetailResult;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.dto.ActivityDTO;
import com.example.lotterysystem.service.dto.ActivityDetailDTO;
import com.example.lotterysystem.service.dto.CreateActivityDTO;
import com.example.lotterysystem.service.dto.PageListDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 活动模块的接口
 */
@RestController
@CrossOrigin
public class ActivityController {
    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private ActivityService activityService;


    /**
     * 创建活动
     * 一、@RequestBody 注解
     * 作用
     * 该注解的主要功能是将 HTTP 请求体里的内容，按照 JSON 或者 XML 等格式进行解析，并绑定到对应的 Java 对象上。它一般用在 Spring MVC 的控制器方法参数上。
     * 使用场景
     * 当你需要处理 POST、PUT 这类请求，且请求中带有 JSON 格式的数据时，就可以使用@RequestBody注解。
     *
     * @param param @RequestBody , 前端传递的是json形式的数据
     * @return
     */
    @RequestMapping("/activity/create")
    public CommonResult<CreateActivityResult> createActivity(@Validated @RequestBody CreateActivityParam param) {
        logger.info("createActivity CreateActivityParam :{}", JacksonUtil.writeValueAsString(param));
        CreateActivityDTO createActivityDTO = activityService.createActivity(param);
        return CommonResult.success(convertToCreateActivityResult(createActivityDTO));
    }

    /**
     * 查询活动列表, 这个有翻页的功能
     */
    @RequestMapping("/activity/find-list")
    public CommonResult<FindActivityListResult> findActivityList(PageListParam param) {
        logger.info("findActivityList FindActivityListParam: {}", JacksonUtil.writeValueAsString(param));
        PageListDTO<ActivityDTO> findActivityListDTO = activityService.findActivityList(param);
        return CommonResult.success(convertToFindActivityResult(findActivityListDTO));
    }

    /**
     * 通过活动id, 查询活动的细节信息:活动id, name, 奖品列表, 人员列表
     *
     * @param activityId
     * @return
     */
    @RequestMapping("/activity-detail/find")
    public CommonResult<GetActivityDetailResult> findActivityDetail(Long activityId) {
        logger.info("getActivityDetail activityId :{}", activityId);
        ActivityDetailDTO detailDTO = activityService.getActivityDetail(activityId);
        return CommonResult.success(convertToGetActivityDetailResult(detailDTO));
    }

    /**
     * 通过活动id, 查询活动的细节信息的返回结果的转换
     *
     * @param detailDTO
     * @return
     */
    private GetActivityDetailResult convertToGetActivityDetailResult(ActivityDetailDTO detailDTO) {
        if (null == detailDTO) {
            throw new ControllerException(ControllerCodeConstants.GET_ACTIVITY_DETAIL_ERROR);
        }
        GetActivityDetailResult result = new GetActivityDetailResult();
        result.setActivityId(detailDTO.getActivityId());
        result.setActivityName(detailDTO.getActivityName());
        result.setDescription(detailDTO.getDesc());
        result.setValid(detailDTO.valid());
        //抽奖顺序: 一,二,三等奖
        result.setPrizes(
                //把detailDTO.getPrizeDTOList()转换到result里面的prizeList
                detailDTO.getPrizeDTOList().stream()
                        .map(prizeDTO -> {
                            GetActivityDetailResult.Prize prize = new GetActivityDetailResult.Prize();
                            prize.setPrizeId(prizeDTO.getId());
                            prize.setName(prizeDTO.getName());
                            prize.setImageUrl(prizeDTO.getImageUrl());
                            prize.setPrice(prizeDTO.getPrice());
                            prize.setDescription(prizeDTO.getDescription());
                            prize.setPrizeTierName(prizeDTO.getTiers().getMessage());
                            prize.setPrizeAmount(prizeDTO.getPrizeAmount());
                            prize.setValid(prizeDTO.valid());
                            return prize;
                        }).toList()
        );

        result.setUsers(
                //把detailDTO.getUserDTOList()转换到result里面的UserList
                detailDTO.getUserDTOList().stream()
                        .map(userDTO -> {
                            GetActivityDetailResult.User user = new GetActivityDetailResult.User();
                            user.setUserId(userDTO.getUserId());
                            user.setUserName(userDTO.getUserName());
                            user.setValid(userDTO.valid());
                            return user;
                        }).toList()
        );

        return result;
    }


    /**
     * 查询活动列表的返回结果的转换, 服务层转换到控制层
     *
     * @param findActivityListDTO
     * @return
     */
    private FindActivityListResult convertToFindActivityResult(PageListDTO<ActivityDTO> findActivityListDTO) {
        if (null == findActivityListDTO) {
            throw new ControllerException(ControllerCodeConstants.FIND_ACTIVITY_LIST_ERROR);
        }
        FindActivityListResult result = new FindActivityListResult();
        result.setTotal(findActivityListDTO.getTotal());
        result.setRecords(findActivityListDTO.getRecords()
                .stream()
                .map(activityDTO -> {
                    //静态内部类, 活动信息属性设置
                    FindActivityListResult.ActivityInfo activityInfo =
                            new FindActivityListResult.ActivityInfo();
                    activityInfo.setActivityId(activityDTO.getActivityId());
                    activityInfo.setActivityName(activityDTO.getActivityName());
                    activityInfo.setDescription(activityDTO.getDescription());
                    activityInfo.setValid(activityDTO.valid());
                    return activityInfo;
                }).toList());
        return result;
    }

    /**
     * 转换返回结果
     *
     * @param createActivityDTO
     * @return
     */
    private CreateActivityResult convertToCreateActivityResult(CreateActivityDTO createActivityDTO) {
        if (null == createActivityDTO) {
            throw new ControllerException(ControllerCodeConstants.CREATE_ACTIVITY_ERROR);
        }
        CreateActivityResult result = new CreateActivityResult();
        result.setActivityId(createActivityDTO.getActivityId());
        return result;
    }


}
