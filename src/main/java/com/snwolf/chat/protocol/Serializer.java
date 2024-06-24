package com.snwolf.chat.protocol;

import cn.hutool.json.JSON;
import com.google.gson.Gson;
import com.snwolf.chat.message.Message;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 6/24/2024
 * @Description: 序列化接口
 */
public interface Serializer {

    <T> T deserialize(Class<T> clazz, byte[] bytes);

    <T> byte[] serialize(T object);

    enum SerializeAlgo implements Serializer{
        JDK {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                // jdk 序列化方式
                T object = null;
                try {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    object = (T) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("反序列化失败", e);
                }
                return object;
            }

            @Override
            public <T> byte[] serialize(T object) {
                // 使用jdk的序列化方式, 获取msg内容的字节数组
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = null;
                try {
                    oos = new ObjectOutputStream(bos);
                    oos.writeObject(object);
                } catch (IOException e) {
                    throw new RuntimeException("序列化失败", e);
                }
                // 得到字节数组
                return bos.toByteArray();
            }
        },

        JSON{
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                return new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                String json = new Gson().toJson(object);
                return json.getBytes(StandardCharsets.UTF_8);
            }
        }
    }
}
