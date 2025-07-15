package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.CreatePrizeByActivityParam;
import com.example.lotterysystem.controller.param.CreateUserByActivityParam;
import com.example.lotterysystem.controller.param.PageListParam;
import com.example.lotterysystem.dao.dataobject.ActivityDO;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.ActivityUserDO;
import com.example.lotterysystem.dao.dataobject.PrizeDO;
import com.example.lotterysystem.dao.mapper.*;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.dto.*;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {
    private static final Logger logger =
            LoggerFactory.getLogger(ActivityServiceImpl.class);

    /**
     * 前缀
     */
    public static final String ACTIVITY_PREFIX = "ACTIVITY_";
    /**
     * 活动有效时间, 私有属性
     */
    private static final long ACTIVITY_EFFECTIVE_TIME = 60 * 60 * 24 * 3;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private ActivityPrizeMapper activityPrizeMapper;

    @Resource
    private ActivityUserMapper activityUserMapper;

    @Resource
    private PrizeMapper prizeMapper;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserMapper userMapper;

    /**
     * 创建活动业务代码的具体实现
     * Transactional(rollbackFor = Exception.class) 是 Spring 框架中用于声明式事务管理的注解，
     * 它表示：当方法内抛出任何类型的异常（包括受检异常和运行时异常）时，Spring 会自动回滚当前事务。
     *
     * @param request 控制层传递的请求
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CreateActivityDTO createActivity(CreateActivityParam request) {
        //校验活动信息:
        checkActivityInfo(request);
        //创建活动
        ActivityDO activityDO = new ActivityDO();
        activityDO.setActivityName(request.getActivityName());
        activityDO.setDescription(request.getDescription());
        activityDO.setStatus(ActivityStatusEnum.RUNNING.name());//创建好的活动肯定是正在进行的
        activityMapper.insert(activityDO);

        //插入关联活动奖品列表
        List<CreatePrizeByActivityParam> prizeParamList = request.getActivityPrizeList();
        //先把活动奖品列表转换为dao层, mapper接口需要的活动奖品参数(XXXXDO)
        List<ActivityPrizeDO> activityPrizeDOList = new ArrayList<>();
        //lambda表达式形式
        prizeParamList.forEach(param -> {//param对象是request里面的一个活动奖品列表的元素
            ActivityPrizeDO activityPrizeDO = new ActivityPrizeDO();
            //把列表的每一个对象都转换为DO对象, 循环遍历
            activityPrizeDO.setActivityId(activityDO.getId());//设置活动id, 不过这个只有activityDO才有
            activityPrizeDO.setPrizeId(param.getPrizeId());
            activityPrizeDO.setPrizeAmount(param.getPrizeAmount());
            activityPrizeDO.setPrizeTiers(param.getPrizeTiers());
            //活动奖品状态枚举类
            activityPrizeDO.setStatus(ActivityPrizeStatusEnum.INIT.name());
            activityPrizeDOList.add(activityPrizeDO);
        });
        activityPrizeMapper.batchInsert(activityPrizeDOList);

        //插入关联活动人员列表
        List<CreateUserByActivityParam> userParamList = request.getActivityUserList();
        List<ActivityUserDO> activityUserDOList = new ArrayList<>();
        userParamList.forEach(param -> {
            ActivityUserDO activityUserDO = new ActivityUserDO();
            activityUserDO.setActivityId(activityDO.getId());
            activityUserDO.setUserId(param.getUserId());
            activityUserDO.setUserName(param.getUserName());
            //活动人员的状态枚举类
            activityUserDO.setStatus(ActivityUserStatusEnum.INIT.name());
            activityUserDOList.add(activityUserDO);
        });
        activityUserMapper.batchInsert(activityUserDOList);

        //查询奖品信息列表, 利用redis, 把常用的奖品和活动缓存到redis中
        List<Long> prizeIds = activityPrizeDOList.stream()
                .map(ActivityPrizeDO::getPrizeId)
                .distinct()
                .toList();
        //用mapper进行查询
        List<PrizeDO> prizeDOList = prizeMapper.batchSelectByIds(prizeIds);
        //缓存到redis
        cacheActivity(convertToActivityDetailDTO(activityDO, activityPrizeDOList, prizeDOList, activityUserDOList));

        //返回活动id
        CreateActivityDTO createActivityDTO = new CreateActivityDTO();
        createActivityDTO.setActivityId(activityDO.getId());//id是自增的, 插入完就赋值了
        return createActivityDTO;
    }

    /**
     * 查询活动列表业务代码的具体实现
     *
     * @param request
     * @return 返回ActivityDTO的列表以及活动棕总数
     */
    @Override
    public PageListDTO<ActivityDTO> findActivityList(PageListParam request) {
        //
        int count = activityMapper.count();
        List<ActivityDO> activities = activityMapper.queryActivitiesByPage(request.offset(),
                request.getPageSize());
        List<ActivityDTO> records = new ArrayList<>();
        for (ActivityDO activityDO : activities) {
            ActivityDTO activityDTO = new ActivityDTO();
            activityDTO.setActivityId(activityDO.getId());
            activityDTO.setActivityName(activityDO.getActivityName());
            activityDTO.setDescription(activityDO.getDescription());
            activityDTO.setStatus(ActivityStatusEnum.forName(activityDO.getStatus()));
            records.add(activityDTO);
        }
        return new PageListDTO<>(count, records);
    }

    /**
     * 获取活动完整信息的业务代码
     *
     * @param activityId
     * @return
     */
    @Override
    public ActivityDetailDTO getActivityDetail(Long activityId) {
        if (null == activityId) {
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_ID_IS_EMPTY);
        }
        //先从redis缓存中获取
        ActivityDetailDTO activityDetailDTO = getActivityFromCache(activityId);
        if (null != activityDetailDTO) {
            logger.info("从redis缓存中获取活动信息成功: {}", JacksonUtil.writeValueAsString(activityDetailDTO));
            return activityDetailDTO;
        }
        //从数据库获取, 并缓存活动数据(加快访问速度, 减少对Mysql的访问次数, 提高效率)
        // 活动表
        ActivityDO aDO = activityMapper.selectById(activityId);

        // 活动奖品表
        List<ActivityPrizeDO> apDOList = activityPrizeMapper.selectByActivityId(activityId);

        // 活动人员表
        List<ActivityUserDO> auDOList = activityUserMapper.selectByActivityId(activityId);

        // 奖品表: 先获取要查询的奖品id
        List<Long> prizeIds = apDOList.stream()
                .map(ActivityPrizeDO::getPrizeId)
                .collect(Collectors.toList());
        //获取活动奖品的完整信息列表
        List<PrizeDO> pDOList = prizeMapper.batchSelectByIds(prizeIds);

        // 整合活动详细信息，存放redis
        activityDetailDTO = convertToActivityDetailDTO(
                aDO, apDOList, pDOList, auDOList);
        cacheActivity(activityDetailDTO);

        logger.info("从数据库获取活动信息成功 :{}", JacksonUtil.writeValueAsString(activityDetailDTO));
        return activityDetailDTO;
    }


    /**
     * 扭转活动状态的业务代码
     * 这是初版代码
     *
     * @param activityId
     * @param activityTargetStatus 活动目标状态
     * @param prizeId
     * @param prizeTargetStatus    活动奖品的目标状态
     */
    @Override
    public void reverseStatus(Long activityId, String activityTargetStatus, Long prizeId, String prizeTargetStatus) {
        if (null == activityId && null == prizeId) {
            logger.error("reverseStatus 要扭转的目标是空! ");
            return;
        }
        boolean update = false;
        //扭转活动奖品状态, 奖品被抽,就会扭转,奖品初始化-> 奖品已经被抽取, 并且数据库中的记录肯定会改变
        //不过每次抽奖请求从前端传递过来, 一般奖品都是被抽了, 除非服务器出现异常, 状态要回溯
        if (null != prizeId && StringUtils.hasText(prizeTargetStatus)) {
            //目标状态不是空字符串, 不是null
            //拿到数据库中的活动奖品
            ActivityPrizeDO apDO = activityPrizeMapper.selectByActivityAndPrizeId(activityId, prizeId);
            //当前的状态
            String currentStatus = apDO.getStatus();

            //当前状态与目标状态不一致才更新, 减少对数据库的操作
            if (!currentStatus.equals(prizeTargetStatus)) {
                //这里可以再继续校验状态在何种状态下可以扭转
                //比如init可以转到completed, 而completed不可以转到init
                activityPrizeMapper.updateStatus(activityId, prizeId, prizeTargetStatus);
                update = true;
            }
        }
        //扭转活动人员状态

        //......

        // 然而活动状态, 抽奖结束, 奖品被抽完,才会扭转状态, 活动正在进行->活动结束
        if (null != activityId && StringUtils.hasText(activityTargetStatus)) {
            //目标状态不是空字符串

            //拿到活动信息
            ActivityDO activityDO = activityMapper.selectById(activityId);
            String currentStatus = activityDO.getStatus();
            //状态不一致,并且奖品都抽完了, 才更新
            if (!currentStatus.equals(activityTargetStatus)
                    && activityPrizeMapper.countInitPrize(activityId) <= 0) {
                activityMapper.updateStatus(activityId, activityTargetStatus);
                update = true;
            }
        }

        //update == true, 说明有状态扭转了, 那么就更新缓存
        //由于创建活动的时候, 将活动信息存放到redis中, 因此修改活动需要更新缓存
        if (update) {
            //通过活动id去缓存活动数据
            cacheActivity(activityId);
        }
    }

    /**
     * //通过活动id去缓存活动完整数据
     *
     * @param activityId
     */
    @Override
    public void cacheActivity(Long activityId) {
        if (null == activityId) {
            logger.warn("要缓存的活动id为空");
            throw new ServiceException(ServiceErrorCodeConstants.CACHE_ACTIVITY_ID_IS_EMPTY);
        }
        //查询表数据: 活动表, 关联奖品表,关联人员表, 奖品信息表
        ActivityDO activityDO = activityMapper.selectById(activityId);
        if (null == activityDO) {
            logger.error("要缓存的活动id有误");
            throw new ServiceException(ServiceErrorCodeConstants.CACHE_ACTIVITY_ID_ERROR);
        }
        //查询出活动奖品列表, 通过活动id, 没有查的那么仔细, 不会找到单个奖品, 但是查的多
        List<ActivityPrizeDO> apDOList = activityPrizeMapper.selectByActivityId(activityId);
        //查询出活动人员列表
        List<ActivityUserDO> auDoList = activityUserMapper.selectByActivityId(activityId);

        //奖品表: 先获取要查询的奖品id
        List<Long> prizeIds = apDOList.stream()
                .map(ActivityPrizeDO::getPrizeId)
                .toList();
        //批量按 ID 查询
        List<PrizeDO> prizeDOList = prizeMapper.batchSelectByIds(prizeIds);

        //整合活动详细信息, 存放到redis中
        cacheActivity(convertToActivityDetailDTO(activityDO, apDOList, prizeDOList, auDoList));
    }

    /**
     * 缓存完整的活动信息
     *
     * @param detailDTO
     */
    @Override
    public void cacheActivity(ActivityDetailDTO detailDTO) {
        //key: ACTIVITY_12 数字是活动id
        //value : ActivityDetailDTO(json)格式
        if (null == detailDTO || null == detailDTO.getActivityId()) {
            logger.warn("要缓存的活动信息不存在");
            return;
        }
        try {
            redisUtil.set(ACTIVITY_PREFIX + detailDTO.getActivityId(),
                    JacksonUtil.writeValueAsString(detailDTO), ACTIVITY_EFFECTIVE_TIME);
        } catch (Exception e) {
            logger.error("缓存活动异常, ActivityDetailDTO = {}", JacksonUtil.writeValueAsString(detailDTO), e);
        }
    }

    /**
     * 根据活动id从redis缓存中获取活动详细信息
     *
     * @param activityId
     * @return
     */
    private ActivityDetailDTO getActivityFromCache(Long activityId) {
        if (null == activityId) {
            logger.warn("获取缓存活动数据的activityId为空");
            return null;
        }
        try {
            String str = (String) redisUtil.get(ACTIVITY_PREFIX + activityId);
            if (!StringUtils.hasText(str)) {
                //没有数据, 是空的
                logger.info("获取的缓存活动数据为空! key = {}", ACTIVITY_PREFIX + activityId);
                return null;
            }
            return JacksonUtil.readValue(str, ActivityDetailDTO.class);
        } catch (Exception e) {
            logger.error("从缓存中获取活动信息异常, key = {}", ACTIVITY_PREFIX + activityId, e);
            return null;
        }
    }

    /**
     * 根据基本DO整合完整的活动信息ActivityDetailDTO
     *
     * @param activityDO          活动基本信息DO
     * @param activityUserDOList  活动关联用户DO列表
     * @param prizeDOList         奖品信息DO列表
     * @param activityPrizeDOList 活动关联奖品DO列表
     * @return 整合后的活动详情DTO
     */
    private ActivityDetailDTO convertToActivityDetailDTO(ActivityDO activityDO,
                                                         List<ActivityPrizeDO> activityPrizeDOList,
                                                         List<PrizeDO> prizeDOList,
                                                         List<ActivityUserDO> activityUserDOList) {
        // 创建目标DTO并设置活动基本信息
        ActivityDetailDTO detailDTO = new ActivityDetailDTO();
        detailDTO.setActivityId(activityDO.getId());
        detailDTO.setActivityName(activityDO.getActivityName());
        detailDTO.setDesc(activityDO.getDescription());
        detailDTO.setStatus(ActivityStatusEnum.forName(activityDO.getStatus()));// 设置活动状态

        //apDo:{prizeId, amount, status}, {prizeId, amount, status} ,要转换成prizeDetailDTO, 本质是ActivityPrizeDO
        //pDo :{prizeID,name,...}, {prizeID,name,...} , {prizeID,name,...}, 本质是PrizeDO
        // 构建活动关联的奖品列表
        // 遍历活动奖品关联表数据，关联基础奖品信息
        List<PrizeDetailDTO> prizeDTOList = activityPrizeDOList.stream()
                .map(apDo -> {
                    PrizeDetailDTO prizeDTO = new PrizeDetailDTO();

                    prizeDTO.setId(apDo.getPrizeId());// 设置奖品ID

                    // 查找关联的基础奖品信息, ActivityPrizeDO与PrizeDO相同的奖品, PrizeDo有更加详细的奖品信息
                    /**
                     * 数据来源：
                     * prizeDOList：数据库中查询出的所有可用奖品列表（基础奖品信息）。
                     * apDO：当前遍历的活动 - 奖品关联对象（包含奖品 ID、数量、状态等活动特有信息）。
                     * 过滤逻辑：
                     * 使用 Stream.filter() 遍历 prizeDOList，查找 PrizeDO 的 ID 等于 apDO.getPrizeId() 的记录。
                     * 例如：若 apDO.getPrizeId() 为 1001，则查找 prizeDOList 中 ID 为 1001 的奖品。
                     * 结果处理：
                     * findFirst() 返回第一个匹配的元素，包装为 Optional<PrizeDO>。
                     * 若未找到匹配项，返回 Optional.empty()。
                     */
                    Optional<PrizeDO> optionalPrizeDO = prizeDOList.stream()
                            .filter(prizeDO -> prizeDO.getId().equals(apDo.getPrizeId()))
                            .findFirst();

                    //如果PrizeDO为空, 不执行当前方法, 不为空才执行
                    // 如果存在基础奖品信息，则设置奖品详细信息
                    /**
                     * 执行流程：
                     * 空值保护：
                     * ifPresent() 仅在 optionalPrizeDO 包含值时执行内部逻辑，避免 NullPointerException。
                     * 属性映射：
                     * 将 PrizeDO 的基础信息（名称、图片、价格、描述）复制到 prizeDTO 中。
                     * 这些信息是所有活动共享的通用奖品信息。
                     * 数据流向：
                     * PrizeDO（基础奖品表数据）→ PrizeDTO（活动详情中的奖品信息）。
                     * 3. 设计目的与业务含义
                     * 数据隔离：
                     * ActivityPrizeDO 存储活动特定的奖品信息（如数量、状态、等级）。
                     * PrizeDO 存储奖品的通用信息（如名称、图片、价格）。
                     * 通过关联避免数据冗余，保证基础信息修改时所有活动同步生效。
                     * 可能的业务场景：
                     * 奖品信息修改（如调整价格、更换图片）不影响历史活动记录。
                     * 同一奖品可被多个活动引用，通过关联表配置不同数量和状态。
                     */
                    optionalPrizeDO.ifPresent(prizeDO -> {
                        prizeDTO.setName(prizeDO.getName());// 设置奖品名称
                        prizeDTO.setImageUrl(prizeDO.getImageUrl());// 设置奖品图片URL
                        prizeDTO.setPrice(prizeDO.getPrice());// 设置奖品价格
                        prizeDTO.setDescription(prizeDO.getDescription());// 设置奖品描述
                    });
                    // 设置奖品在活动中的特有属性
                    prizeDTO.setTiers(ActivityPrizeTiersEnum.forName(apDo.getPrizeTiers()));// 奖品等级
                    prizeDTO.setPrizeAmount(apDo.getPrizeAmount());// 奖品数量
                    prizeDTO.setStatus(ActivityPrizeStatusEnum.forName(apDo.getStatus()));// 奖品状态
                    return prizeDTO;
                }).toList();
        detailDTO.setPrizeDTOList(prizeDTOList);

        /**
         * 构建活动人员列表信息
         */
        List<UserDetailDTO> userDTOList = activityUserDOList.stream()
                .map(auDo -> {
                    UserDetailDTO userDTO = new UserDetailDTO();
                    userDTO.setUserId(auDo.getUserId());
                    userDTO.setUserName(auDo.getUserName());
                    userDTO.setStatus(ActivityUserStatusEnum.forName(auDo.getStatus()));
                    return userDTO;
                }).toList();
        detailDTO.setUserDTOList(userDTOList);
        return detailDTO;
    }

    /**
     * 校验活动创建参数的合法性
     *
     * @param param
     */
    private void checkActivityInfo(CreateActivityParam param) {
        // 校验参数是否为空
        if (null == param) {
            //参数不合法时抛出自定义的服务器异常
            throw new ServiceException(ServiceErrorCodeConstants.CREATE_ACTIVITY_INFO_IS_EMPTY);
        }

        //人员id在人员表是否存在, 可能出现重复的人员, 不在人员表中的人员
        //1 2 2 3 -> 1 2 3
        // 1. 校验活动关联的用户ID是否存在于用户表
        // 提取用户ID列表并去重（避免重复查询相同用户）
        /**
         * 数据来源：从活动创建参数param中获取activityUserList（活动关联的用户列表）
         * Stream 处理流程：
         * stream()：将列表转换为流，以便进行流式处理
         * map(CreateUserByActivityParam::getUserId)：通过方法引用提取每个用户参数对象中的userId
         * distinct()：对提取的用户 ID 进行去重处理，避免重复的用户 ID
         * collect(Collectors.toList())：将处理后的流转换回 List 集合
         */
        List<Long> userIds = param.getActivityUserList()
                .stream()
                .map(CreateUserByActivityParam::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> existUserIds = userMapper.selectExistByIds(userIds);

        // 查询数据库中存在的用户ID
        if (CollectionUtils.isEmpty(existUserIds)) {
            // 如果没有任何有效用户ID，抛出异常
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_ERROR);
        }
        // 校验每个用户ID是否都存在于数据库中
        userIds.forEach(id -> {
            if (!existUserIds.contains(id)) {
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_ERROR);
            }
        });

        // 2. 校验活动关联的奖品ID是否存在于奖品表
        // 提取奖品ID列表并去重
        /**
         * 数据来源：从活动创建参数param中获取activityPrizeList（活动关联的奖品列表）
         * Stream 处理流程：
         * stream()：将列表转换为流
         * map(CreatePrizeByActivityParam::getPrizeId)：提取每个奖品参数对象中的prizeId
         * distinct()：对提取的奖品 ID 进行去重
         * toList()：将流转换为 List 集合（Java 16 + 新特性）
         */
        List<Long> prizeIds = param.getActivityPrizeList()
                .stream()
                .map(CreatePrizeByActivityParam::getPrizeId)
                .distinct()
                .toList();
        // 查询数据库中存在的奖品ID
        List<Long> existPrizeIds = prizeMapper.selectExistByIds(prizeIds);

        /// 如果没有任何有效奖品ID，抛出异常
        if (CollectionUtils.isEmpty(existPrizeIds)) {
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_ERROR);
        }

        // 校验每个奖品ID是否都存在于数据库中
        prizeIds.forEach(id -> {
            if (!existPrizeIds.contains(id)) {
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_ERROR);
            }
        });

        // 3. 校验用户数量是否足够分配奖品
        //人员数量大于奖品数量
        //2个奖品 2 1

        // 获取参与活动的用户总数
        int userAmount = param.getActivityUserList().size();

        // 计算奖品总数量（每种奖品可能有多个）
        /**
         * 数据来源：从活动创建参数中获取activityPrizeList
         * Stream 处理流程：
         * stream()：将奖品列表转换为流
         * mapToLong(CreatePrizeByActivityParam::getPrizeAmount)：
         * 将每个奖品参数对象转换为long类型的奖品数量
         * mapToLong专门用于处理基本类型，比普通map更高效
         * sum()：对流中的所有奖品数量进行求和计算
         */
        long prizeAmount = param.getActivityPrizeList()
                .stream()
                .mapToLong(CreatePrizeByActivityParam::getPrizeAmount)
                .sum();

        // 确保用户数量不少于奖品数量，避免奖品过剩无法分配
        if (userAmount < prizeAmount) {
            throw new ServiceException(ServiceErrorCodeConstants.USER_PRIZE_AMOUNT_ERROR);
        }

        //校验活动奖品等级有效性
        //活动奖品等级枚举类的方法调用
        // 4. 校验活动奖品等级是否有效
        // 遍历每个奖品，验证其奖品等级是否存在于枚举值中
        param.getActivityPrizeList().forEach(prize -> {
            if (null == ActivityPrizeTiersEnum.forName(prize.getPrizeTiers())) {
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_TIERS_ERROR);
            }
        });
    }
}
