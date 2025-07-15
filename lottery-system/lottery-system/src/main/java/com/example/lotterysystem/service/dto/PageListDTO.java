package com.example.lotterysystem.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 页面列表信息, 是服务层返回结果
 */
@Data
public class PageListDTO<T> implements Serializable {
    /**
     * 总数
     */
    private Integer total;

    /**
     * 展示到页面上的具体数据
     */
    private List<T> records;

    public PageListDTO(Integer total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}
