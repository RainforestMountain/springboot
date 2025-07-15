package com.example.lotterysystem.dao.dataobject;

import lombok.Data;

/**
 * 加密类, 说明这个属性要加密
 * 凡是此实体类的数据都表⽰需要加解密的
 * 回归最初的问题，我们对⼿机号进⾏存储时，要先将⼿机号加密，如果要拿出使⽤时，还要进⾏⼀次
 * 解密操作。为了不让每次⼿动去加密解密，决定使⽤ Mybatis 的 TypeHandler 来解决。
 * TypeHandler : 简单理解就是当处理某些特定字段时，我们可以实现⼀些⽅法，让 Mybatis 遇到这些
 * 特定字段可以⾃动运⾏处理。
 */
@Data
public class Encrypt {
    private String value;//加密对象的属性值

    public Encrypt() {

    }

    public Encrypt(String value) {
        this.value = value;
    }
}
