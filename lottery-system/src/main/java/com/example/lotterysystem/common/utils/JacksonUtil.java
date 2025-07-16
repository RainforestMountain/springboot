package com.example.lotterysystem.common.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Callable;
@Component
public class JacksonUtil {
    /**
     * 私有构造函数，禁止外部实例化该工具类
     */
    private JacksonUtil() {

    }

    /**
     * 静态代码块,单例
     * 注：此处"单例模式"描述不准确，实际是静态单例实例
     */

    private final static ObjectMapper OBJECT_MAPPER;

    /**
     * 静态代码块：在类加载时初始化OBJECT_MAPPER实例
     * 确保ObjectMapper全局唯一，避免重复创建带来的性能开销
     */
    static {
        OBJECT_MAPPER = new ObjectMapper();
    }

    /**
     * 获取ObjectMapper实例的私有方法
     *
     * @return 初始化好的ObjectMapper实例
     */
    private static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 通用反序列化尝试方法（简化版）
     *
     * @param parser 包含反序列化逻辑的Callable函数式接口
     * @param <T>    反序列化结果类型
     * @return 反序列化结果，异常时抛出JsonParseException
     */
    private static <T> T tryParse(Callable<T> parser) {
        return tryParse(parser, JacksonException.class);
    }

    /**
     * 通用反序列化尝试方法（完整逻辑版）
     *
     * @param parser 封装反序列化逻辑的Callable函数式接口
     * @param check  需要检查的异常基类，用于判断是否转换为JsonParseException
     * @param <T>    反序列化目标类型
     * @return 反序列化结果，非预期异常时转换为IllegalAccessException
     * @throws JsonParseException     当捕获到指定类型的异常时抛出
     * @throws IllegalAccessException 当捕获到非指定类型异常时抛出
     */
    private static <T> T tryParse(Callable<T> parser, Class<? extends Exception> check) {

        try {
            return parser.call();// 执行反序列化操作
        } catch (Exception var4) {
            // 检查异常类型是否为指定异常的子类
            if (check.isAssignableFrom(var4.getClass())) {
                // 是指定异常类型，转换为JsonParseException并保留原始消息
                throw new org.springframework.boot.json.JsonParseException(var4);
            }
            // 非指定异常类型
            throw new IllegalStateException(var4);
        }
    }

    /**
     * 将JSON字符串反序列化为指定类型的Java对象
     *
     * @param content   待解析的JSON字符串
     * @param valueType 反序列化目标类型的Class对象
     * @param <T>       反序列化目标类型
     * @return 解析后的Java对象，解析失败时抛出异常
     */
    public static <T> T readValue(String content, Class<T> valueType) {
        return JacksonUtil.tryParse(() -> JacksonUtil.getObjectMapper().readValue(content, valueType));
    }

    /**
     * 将JSON字符串反序列化为指定元素类型的List集合
     * 1. JacksonUtil.tryParse(Callable<T> parser)
     * 作用：封装反序列化逻辑并处理异常
     * 参数：
     * () -> ... 是一个 Lambda 表达式，实现Callable<T>接口的call()方法，用于执行反序列化操作
     * 内部逻辑：
     * 调用parser.call()执行反序列化，若捕获到异常则根据类型转换后抛出
     * 2. JacksonUtil.getObjectMapper()
     * 作用：获取全局唯一的ObjectMapper实例（单例模式）
     * 实现：
     * 直接返回静态变量OBJECT_MAPPER，该实例在静态代码块中初始化
     * 意义：
     * 确保ObjectMapper实例全局唯一，避免重复创建带来的性能开销
     * 3. ObjectMapper.readValue(String content, JavaType valueType)
     * 作用：将 JSON 字符串反序列化为 Java 对象
     * 参数：
     * content：待解析的 JSON 字符串
     * valueType：反序列化的目标类型（通过JavaType表示）
     * 底层逻辑：
     * 解析 JSON 内容并根据valueType创建对应的 Java 对象实例
     * 4. ObjectMapper.getTypeFactory()
     * 作用：获取TypeFactory实例，用于构建复杂类型
     * TypeFactory的作用：
     * 处理 Java 类型的表示和转换，尤其是泛型类型的构建
     * 5. TypeFactory.constructParametricType(Class<?> rawType, Type... typeParameters)
     * 作用：构建参数化类型（即带泛型参数的类型）
     * 参数：
     * rawType：原始类型（如List.class）
     * typeParameters：泛型参数类型（如String.class）
     * 示例：
     * constructParametricType(List.class, String.class) 表示 List<String> 类型
     * 在此处的作用：
     * 构建List<parameterClasses>类型，其中parameterClasses是传入的元素类型（如User.class）
     *
     * @param content          待解析的JSON字符串
     * @param parameterClasses List集合中元素的Class类型
     * @param <T>              反序列化目标类型（List<?>）
     * @return 解析后的List集合，解析失败时抛出异常
     */
    public static <T> T readListValue(String content, Class<?> parameterClasses) {
        return JacksonUtil.tryParse(() -> JacksonUtil
                .getObjectMapper()
                .readValue(content, JacksonUtil
                        .getObjectMapper()
                        .getTypeFactory()
                        .constructParametricType(List.class, parameterClasses)));
    }

    /**
     * 将Java对象序列化为JSON字符串
     *
     * @param value 待序列化的Java对象
     * @return 序列化后的JSON字符串，序列化失败时抛出异常
     */
    public static String writeValueAsString(Object value) {
        return JacksonUtil.tryParse(() -> JacksonUtil.getObjectMapper().writeValueAsString(value));
    }
}
