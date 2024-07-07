package com.snwolf.chat.protocol;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.lang.reflect.Type;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 7/7/2024
 * @Description:
 */
@Slf4j
public class TestGson {

    @Test
    public void testGson(){
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Serializer.ClassCodec()).create();
        System.out.println(gson.toJson(String.class));
    }
}
