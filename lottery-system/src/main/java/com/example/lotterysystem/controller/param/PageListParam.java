package com.example.lotterysystem.controller.param;

import lombok.Data;

import java.io.Serializable;

/**
 * 奖品列表展示接口的参数, 除非参数绑定,一般都需要前后端参数一致
 */
@Data
public class PageListParam implements Serializable {
    /**
     * 当前页
     */
    private Integer currentPage = 1;

    /**
     * 每页中的记录数
     */
    private Integer pageSize = 10;

    /**
     * 在分页查询中，offset() 方法用于计算 数据偏移量，
     * 即从数据库中获取数据时的起始位置。它是分页查询的核心参数之一，
     * 通常与 pageSize（每页记录数）配合使用。
     * @return
     */
    public Integer offset() {
        return (currentPage - 1) * pageSize;
    }
}
