package com.example.lotterysystem.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    /**
     * StringRedisTemplate继承RedisTemplate<String, String>, 两种的区别在于序列化的方式不同
     * 这里选用StringRedisTemplate,能够避免乱码问题
     */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 判断这个key是否存在
     *
     * @param key
     * @return
     */
    public boolean hasKey(String key) {
        try {
            return stringRedisTemplate.hasKey(key);
        } catch (Exception e) {
            logger.error("error occurred in RedisUtil.hasKey({}) ----> ", key, e);
            return false;
        }
    }

    /**
     * expire:到期
     * 指定key的过期时间为time, 单位是秒
     *
     * @param key 键
     * @return true 成功 false 不成功
     */
    public boolean setExpire(String key, long time) {
        try {
            if (time > 0) {
                stringRedisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            logger.error("error occurred in RedisUtil.setExpire({}, {} ) ----> ", key, time, e);
        }
        return false;
    }

    /**
     * 根据key获取过期时间
     *
     * @param key 键, 不能为null
     * @return 时间(秒) 返回0表示永远不过期
     */
    public long getExpire(String key) {
        try {
            if (key == null) {
                throw new NullPointerException("key 为空");
            }
            return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("error occurred in RedisUtil.getExpire({}) ----> ", key, e);
        }
        return Long.MAX_VALUE;
    }

    /**
     * 删除key,
     *
     * @param key 可以传递一个或者多个, 类型是List<String>,
     *            在方法内部，这些参数会被视为一个数组。如果你想对这些参数进行类型判断，
     *            可以使用 instanceof 操作符来检查每个参数的类型。
     */
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                stringRedisTemplate.delete(key[0]);
                logger.info("redis delete key :{}", key[0]);
            } else {
                stringRedisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    /**
     * redis存储格式 key = value
     * 获取指定键的值
     *
     * @param key 这里是 String 类型
     * @return 值
     */
    public Object get(String key) {
        logger.info("redis get key : {}", key);
        return key == null ? null : stringRedisTemplate.opsForValue().get(key);
    }

    public boolean set(String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
            logger.info("redis set key :{}", key);
            return true;
        } catch (Exception e) {
            logger.error("error occurred in RedisUtil.set({}, {}) ---->", key, value, e);
        }
        return false;
    }

    /**
     * 设置指定键的值, 并设置过期时间
     *
     * @param key
     * @param value
     * @param time  单位为秒, time > 0, 合格的过期时间, time <= 0, 将设置无限期
     * @return true 成功, false 失败
     */
    public boolean set(String key, String value, long time) {
        try {
            if (time > 0) {
                stringRedisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);//无限期
            }
            logger.info("redis set key :{}", key);
            return true;
        } catch (Exception e) {
            logger.error("error occurred in RedisUtil.set({}, {}, {}) ---->", key, value, time, e);
        }
        return false;
    }
}
