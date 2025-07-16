package com.example.lotterysystem.service.activitystatus.operator;

import com.example.lotterysystem.service.dto.ActivityStatusConvertDTO;
import lombok.Data;

/**
 * 这也是策略模式
 * 责任链模式中的处理者对象集合, 责任链模式的一环
 * 这是父类, 实现了继承和多态, 类型运行时才确定
 */
@Data
public abstract class AbstractActivityOperator {
    /**
     * 获取处理器的序列: 1, 2
     * 这里是奖品处理器优先级==人员处理器优先级 > 活动处理器优先级
     */
    public abstract Integer sequence();

    /**
     * 判断处理器是否要执行转换操作, 是否符合转换条件
     *
     * @param statusConvertDTO 状态转换请求对象
     * @return
     */
    public abstract boolean needConvert(ActivityStatusConvertDTO statusConvertDTO);

    /**
     * 真正执行转换状态操作, 是父类方法, 具体处理器的类型运行时才知道
     *
     * @param statusConvertDTO 状态转换请求对象
     * @return
     */
    public abstract boolean convertStatus(ActivityStatusConvertDTO statusConvertDTO);
}
