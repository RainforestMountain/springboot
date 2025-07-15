package com.example.lotterysystem.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class JacksonUtilTest {
    // 测试用的简单Java对象
    static class User {
        private String name;
        private int age;

        public User() {
        }

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "User{" +
                    "age=" + age +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    @Test
    void testWriteValueAsString() {
//        User user = new User("zhangsan", 25);
//        String json = JacksonUtil.writeValueAsString(user);
//        System.out.println(json);

        //序列化list
        List<User> userList = new ArrayList<>();
        userList.add(new User("zhangsan", 25));
        userList.add(new User("lisi", 20));

        String listJson = JacksonUtil.writeValueAsString(userList);
        System.out.println(listJson);
    }

    @Test
    void readValue() {
        String json = "{\"name\":\"zhangsan\",\"age\":25}";
        User user = JacksonUtil.readValue(json, User.class);
        System.out.println(user.toString());
    }

    @Test
    void readListValue() {
        String listjson = "[{\"name\":\"zhangsan\",\"age\":25},{\"name\":\"lisi\",\"age\":20}]";
        List<User> userList = JacksonUtil.readListValue(listjson, User.class);
        for (User user : userList) {
            System.out.println(user);
        }
    }

//    //测试序列化异常处理
//    @Test
//    void testWriteValueAsStringException() {
//        //创建一个不可序列化对象
//        //匿名内部类
//        Object invalidObject = new Object() {
//            @Override
//            public String toString() {
//                return "Non-serializable object";
//            }
//        };
//
//        try {
//            JacksonUtil.writeValueAsString(invalidObject);
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }
//
//    // 测试反序列化异常处理（不匹配的类型）
//    @Test
//    void testReadValueMismatchedType() {
//        String json = "{\"name\":\"John\",\"age\":30}";
//
//        try {
//            Integer i = JacksonUtil.readValue(json, Integer.class);
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }

}